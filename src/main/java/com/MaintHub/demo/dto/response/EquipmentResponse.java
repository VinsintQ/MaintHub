package com.MaintHub.demo.dto.response;

import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.model.Equipment;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EquipmentResponse {
    private Long id;
    private String name;
    private String serialNumber;
    private String description;
    private String location;
    private EquipmentStatus status;
    private Integer conditionLevel;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;
    private LocalDateTime createdAt;
    private Long categoryId;
    private String categoryName;

    public static EquipmentResponse from(Equipment equipment) {
        EquipmentResponse response = new EquipmentResponse();
        response.setId(equipment.getId());
        response.setName(equipment.getName());
        response.setSerialNumber(equipment.getSerialNumber());
        response.setDescription(equipment.getDescription());
        response.setLocation(equipment.getLocation());
        response.setStatus(equipment.getStatus());
        response.setConditionLevel(equipment.getConditionLevel());
        response.setPurchaseDate(equipment.getPurchaseDate());
        response.setWarrantyExpiryDate(equipment.getWarrantyExpiryDate());
        response.setCreatedAt(equipment.getCreatedAt());
        response.setCategoryId(equipment.getCategory().getId());
        response.setCategoryName(equipment.getCategory().getName());
        return response;
    }
}
