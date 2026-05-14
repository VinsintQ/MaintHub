package com.MaintHub.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SparePartRequestCreateRequest {
    @NotNull
    private Long maintenanceTaskId;

    @NotBlank
    private String partName;

    @NotNull
    @Positive
    private Integer quantity;

    @PositiveOrZero
    private BigDecimal estimatedPrice;
}
