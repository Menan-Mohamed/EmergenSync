package com.example.backend.enums;

public enum VehicleStatus {
    AVAILABLE,
    ON_ROUTE,
    ON_ROUTE_PENDING,   // 🔑 claimed but not yet assigned
    BUSY,
    MAINTENANCE
}
