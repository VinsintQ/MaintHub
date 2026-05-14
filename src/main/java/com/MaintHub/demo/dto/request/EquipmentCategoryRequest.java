package com.MaintHub.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EquipmentCategoryRequest {
    @NotBlank
    private String name;

    private String description;

    private boolean requiresRegularInspection;

    @Positive
    private Integer inspectionIntervalDays;
}
