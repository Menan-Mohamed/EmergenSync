from fastapi import FastAPI
import threading
import time
import requests
import redis
from queue import Queue

app = FastAPI()

# Redis connection
redis_client = redis.Redis(
    host="localhost",
    port=6379,
    decode_responses=True
)

OSRM_URL = "https://router.project-osrm.org/route/v1/driving"
SPRING_URL = "http://localhost:8080/api/responder/vehicle"

# Queue to hold simulation tasks
simulation_queue = Queue()

# Number of worker threads
NUM_WORKERS = 3  # adjust based on your network capability


def get_route(start_lat, start_lon, end_lat, end_lon):
    url = f"{OSRM_URL}/{start_lon},{start_lat};{end_lon},{end_lat}"
    params = {"overview": "simplified", "geometries": "geojson"}

    try:
        response = requests.get(url, params=params, timeout=10)
        response.raise_for_status()
        data = response.json()
    except requests.RequestException as e:
        print(f"OSRM request failed: {e}")
        return []
    except ValueError:
        print("Error parsing OSRM response")
        return []

    if "routes" not in data or len(data["routes"]) == 0:
        print("No route found in OSRM response")
        return []

    coords = data["routes"][0]["geometry"]["coordinates"]
    return [(c[1], c[0]) for c in coords]


def simulate_vehicle_task(vehicle_id, start_lat, start_lon, end_lat, end_lon):
    route = get_route(start_lat, start_lon, end_lat, end_lon)

    # Ensure destination is the last point
    if not route or route[-1] != (end_lat, end_lon):
        route.append((end_lat, end_lon))

    for lat, lon in route:
        print(f"Vehicle {vehicle_id} moving to: lat={lat}, lon={lon}")

        # Update Redis
        redis_client.hset(
            f"vehicle:{vehicle_id}",
            mapping={"lat": lat, "lon": lon, "timestamp": int(time.time())}
        )

        # Call Spring Boot backend
        try:
            response = requests.put(
                f"{SPRING_URL}/{vehicle_id}/location",
                params={"latitude": lat, "longitude": lon},
                timeout=5
            )
            print(f"Spring response: {response.status_code} - {response.text}")
        except requests.RequestException as e:
            print(f"Error calling Spring Boot: {e}")

        time.sleep(2)  # simulate movement delay

    # Remove Redis key
    redis_client.delete(f"vehicle:{vehicle_id}")
    print(f"Vehicle {vehicle_id} has arrived at destination.")


def worker():
    while True:
        task = simulation_queue.get()
        if task is None:
            break  # allows stopping the worker
        try:
            simulate_vehicle_task(*task)
        except Exception as e:
            print(f"Error simulating vehicle {task[0]}: {e}")
        finally:
            simulation_queue.task_done()


# Start worker threads
for _ in range(NUM_WORKERS):
    t = threading.Thread(target=worker, daemon=True)
    t.start()


@app.post("/simulate")
def start_simulation(data: dict):
    print(f"Received simulation request: {data}")
    simulation_queue.put((
        data["vehicleId"],
        data["startLat"],
        data["startLon"],
        data["endLat"],
        data["endLon"]
    ))
    return {"status": "simulation queued"}
