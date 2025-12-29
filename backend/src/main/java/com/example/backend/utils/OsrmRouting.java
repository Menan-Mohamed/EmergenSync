package com.example.backend.utils;

import com.example.backend.dtos.Point;
import com.example.backend.dtos.response.OsrmRouteResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OsrmRouting {

    private final RestClient restClient;
    private static final String OSRM_BASE_URL = "http://router.project-osrm.org";

    public OsrmRouting() {
        // Create a RestClient that doesn't accept gzip encoding
        this.restClient = RestClient.builder()
                .defaultHeader("Accept-Encoding", "identity") // Disable gzip
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    public OsrmRouteResponse getRoute(double originLon, double originLat,
                                      double destLon, double destLat) {
        String url = String.format("%s/route/v1/driving/%f,%f;%f,%f?overview=simplified&geometries=geojson",
                OSRM_BASE_URL, originLon, originLat, destLon, destLat);

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(OsrmRouteResponse.class);
    }

    public List<Point> getBestRoutePoints(double originLon, double originLat,
                                          double destLon, double destLat) {
        OsrmRouteResponse response = getRoute(originLon, originLat, destLon, destLat);

        if (response.getRoutes() == null || response.getRoutes().isEmpty()) {
            return Collections.emptyList();
        }

        List<List<Double>> coords = response.getRoutes().get(0).getGeometry().getCoordinates();

        return coords.stream()
                .map(coord -> new Point(coord.get(0), coord.get(1)))
                .collect(Collectors.toList());
    }
}
