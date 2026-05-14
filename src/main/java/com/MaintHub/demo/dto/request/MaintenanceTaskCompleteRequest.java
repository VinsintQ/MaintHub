package com.MaintHub.demo.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaintenanceTaskCompleteRequest {
    private String repairNotes;

    @PositiveOrZero
    private BigDecimal actualCost;
}
