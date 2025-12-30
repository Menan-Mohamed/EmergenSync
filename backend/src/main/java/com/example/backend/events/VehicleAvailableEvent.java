package com.example.backend.events;

public class VehicleAvailableEvent {
    private final Integer vehicleId;

    public VehicleAvailableEvent(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }
}