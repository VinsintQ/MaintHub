package com.MaintHub.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DamageReportAssignRequest {
    @NotNull
    private Long technicianId;

    @PositiveOrZero
    private BigDecimal estimatedCost;
}
