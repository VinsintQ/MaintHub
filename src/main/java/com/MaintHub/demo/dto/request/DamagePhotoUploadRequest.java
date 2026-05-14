package com.MaintHub.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DamagePhotoUploadRequest {
    @NotBlank
    private String damagePhotoUrl;
}
