package com.example.backend.events;

import lombok.Getter;

@Getter
public class VehicleCreatedEvent {
    private final Integer vehicleId;

    public VehicleCreatedEvent(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

}