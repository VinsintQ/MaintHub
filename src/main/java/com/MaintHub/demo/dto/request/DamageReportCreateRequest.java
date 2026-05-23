package com.MaintHub.demo.dto.request;

import com.MaintHub.demo.enums.DamageSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DamageReportCreateRequest {
    @NotNull
    private Long equipmentId;

    @NotBlank
    private String description;

    @NotNull
    private DamageSeverity severity;
}
