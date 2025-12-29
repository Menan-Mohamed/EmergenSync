package com.example.backend.dtos.response;

import lombok.Data;

import java.util.List;

@Data
public class OsrmRouteResponse {
    private String code;
    private List<Route> routes;

    @Data
    public static class Route {
        private double distance; // in meters
        private double duration; // in seconds
        private Geometry geometry;
    }

    @Data
    public static class Geometry {
        private String type;
        private List<List<Double>> coordinates;
    }
}