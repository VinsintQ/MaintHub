package com.MaintHub.demo.dto.request;

import com.MaintHub.demo.enums.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String serialNumber;

    private String description;

    private String location;

    private EquipmentStatus status;

    @Positive
    private Integer conditionLevel;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    @NotNull
    private Long categoryId;
}
