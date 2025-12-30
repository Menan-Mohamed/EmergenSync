package com.example.backend.events;

public class IncidentCreatedEvent {
    private final Integer incidentId;

    public IncidentCreatedEvent(Integer incidentId) {
        this.incidentId = incidentId;
    }

    public Integer getIncidentId() {
        return incidentId;
    }
}