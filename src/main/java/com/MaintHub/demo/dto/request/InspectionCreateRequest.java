package com.MaintHub.demo.dto.request;

import com.MaintHub.demo.enums.InspectionResult;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InspectionCreateRequest {
    @NotNull
    private Long maintenanceTaskId;

    @NotNull
    private InspectionResult result;

    private String notes;
}
