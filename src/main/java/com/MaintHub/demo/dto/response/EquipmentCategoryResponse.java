package com.MaintHub.demo.dto.response;

import com.MaintHub.demo.model.EquipmentCategory;
import lombok.Data;

@Data
public class EquipmentCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private boolean requiresRegularInspection;
    private Integer inspectionIntervalDays;

    public static EquipmentCategoryResponse from(EquipmentCategory category) {
        EquipmentCategoryResponse response = new EquipmentCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setRequiresRegularInspection(category.isRequiresRegularInspection());
        response.setInspectionIntervalDays(category.getInspectionIntervalDays());
        return response;
    }
}
