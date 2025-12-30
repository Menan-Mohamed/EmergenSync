package com.example.backend.dtos;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class AssignmentDto {

    private int vehicleId;

    private int incidentId;

    private LocalDateTime assignedAt;

    private LocalDateTime solvedAt;
}
