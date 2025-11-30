package com.example.backend.utils;

public class HaversineFormula {

    public double haversine(double latitude1, double longitude1, double latitude2, double longitude2) {

        final double earthRadius = 6371; //km
        double deltaLatitudeRadians = Math.toRadians(latitude2 - latitude1);
        double deltaLongitudeRadians = Math.toRadians(longitude2 - longitude1);

        double haversineIntermediateValue = Math.sin(deltaLatitudeRadians/2) * Math.sin(deltaLatitudeRadians/2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.sin(deltaLongitudeRadians/2) * Math.sin(deltaLongitudeRadians/2);

        double angularDistanceInRadians = 2 * Math.atan2(Math.sqrt(haversineIntermediateValue), Math.sqrt(1 - haversineIntermediateValue));

        return earthRadius * angularDistanceInRadians;
    }
}
