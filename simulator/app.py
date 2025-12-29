from fastapi import FastAPI
import threading
import time
import requests
import redis

app = FastAPI()

# Redis connection
redis_client = redis.Redis(
    host="localhost",
    port=6379,
    decode_responses=True
)

OSRM_URL = "https://router.project-osrm.org/route/v1/driving"
SPRING_URL = "http://localhost:8080/api/responder/vehicle"



def get_route(start_lat, start_lon, end_lat, end_lon):
    url = f"{OSRM_URL}/{start_lon},{start_lat};{end_lon},{end_lat}"
    params = {
        "overview": "simplified",
        "geometries": "geojson"
    }

    response = requests.get(url, params=params)
    response.raise_for_status()

    try:
        data = response.json()
    except ValueError:
        print("Error parsing OSRM response:", response.text)
        return []

    if "routes" not in data or len(data["routes"]) == 0:
        print("No route found in OSRM response")
        return []

    coords = data["routes"][0]["geometry"]["coordinates"]
    # OSRM returns [lon, lat], convert to (lat, lon)
    return [(c[1], c[0]) for c in coords]


def simulate_vehicle(vehicle_id, start_lat, start_lon, end_lat, end_lon):
    route = get_route(start_lat, start_lon, end_lat, end_lon)

    # Make sure the incident point is the last point
    if not route or route[-1] != (end_lat, end_lon):
        route.append((end_lat, end_lon))

    for lat, lon in route:
        print(f"Vehicle {vehicle_id} moving to: lat={lat}, lon={lon}")

        # Update Redis
        redis_client.hset(
            f"vehicle:{vehicle_id}",
            mapping={
                "lat": lat,
                "lon": lon,
                "timestamp": int(time.time())
            }
        )

        # Call Spring Boot backend to update vehicle location
        try:
            response = requests.put(
                f"{SPRING_URL}/{vehicle_id}/location",
                params={"latitude": lat, "longitude": lon},
                timeout=5
            )
            print(f"Spring response: {response.status_code} - {response.text}")
        except Exception as e:
            print("Error calling Spring Boot:", e)

        time.sleep(2)  # simulate movement delay

    # Remove Redis key when arrived
    redis_client.delete(f"vehicle:{vehicle_id}")
    print(f"Vehicle {vehicle_id} has arrived at destination.")


@app.post("/simulate")
def start_simulation(data: dict):
    print(f"Received simulation request: {data}")
    thread = threading.Thread(
        target=simulate_vehicle,
        args=(
            data["vehicleId"],
            data["startLat"],
            data["startLon"],
            data["endLat"],
            data["endLon"]
        ),
        daemon=True
    )
    thread.start()

    return {"status": "simulation started"}
