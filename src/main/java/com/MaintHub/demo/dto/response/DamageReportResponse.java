package com.MaintHub.demo.dto.response;

import com.MaintHub.demo.enums.DamageReportStatus;
import com.MaintHub.demo.enums.DamageSeverity;
import com.MaintHub.demo.model.DamageReport;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DamageReportResponse {
    private Long id;
    private String description;
    private DamageSeverity severity;
    private DamageReportStatus status;
    private String damagePhotoUrl;
    private LocalDateTime reportedAt;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentSerialNumber;
    private UserSummaryResponse reportedBy;
    private Long maintenanceTaskId;

    public static DamageReportResponse from(DamageReport report) {
        DamageReportResponse response = new DamageReportResponse();
        response.setId(report.getId());
        response.setDescription(report.getDescription());
        response.setSeverity(report.getSeverity());
        response.setStatus(report.getStatus());
        response.setDamagePhotoUrl(report.getDamagePhotoUrl());
        response.setReportedAt(report.getReportedAt());
        response.setEquipmentId(report.getEquipment().getId());
        response.setEquipmentName(report.getEquipment().getName());
        response.setEquipmentSerialNumber(report.getEquipment().getSerialNumber());
        response.setReportedBy(UserSummaryResponse.from(report.getReportedBy()));
        if (report.getMaintenanceTask() != null) {
            response.setMaintenanceTaskId(report.getMaintenanceTask().getId());
        }
        return response;
    }
}
