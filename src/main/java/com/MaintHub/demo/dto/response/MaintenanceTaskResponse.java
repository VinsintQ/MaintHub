package com.MaintHub.demo.dto.response;

import com.MaintHub.demo.enums.MaintenanceStatus;
import com.MaintHub.demo.model.MaintenanceTask;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaintenanceTaskResponse {
    private Long id;
    private MaintenanceStatus status;
    private String repairNotes;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private boolean repairCostApproved;
    private LocalDateTime costApprovedAt;
    private Long damageReportId;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentSerialNumber;
    private UserSummaryResponse technician;
    private Long inspectionId;

    public static MaintenanceTaskResponse from(MaintenanceTask task) {
        MaintenanceTaskResponse response = new MaintenanceTaskResponse();
        response.setId(task.getId());
        response.setStatus(task.getStatus());
        response.setRepairNotes(task.getRepairNotes());
        response.setEstimatedCost(task.getEstimatedCost());
        response.setActualCost(task.getActualCost());
        response.setStartedAt(task.getStartedAt());
        response.setCompletedAt(task.getCompletedAt());
        response.setRepairCostApproved(task.isRepairCostApproved());
        response.setCostApprovedAt(task.getCostApprovedAt());
        response.setDamageReportId(task.getDamageReport().getId());
        response.setEquipmentId(task.getEquipment().getId());
        response.setEquipmentName(task.getEquipment().getName());
        response.setEquipmentSerialNumber(task.getEquipment().getSerialNumber());
        response.setTechnician(UserSummaryResponse.from(task.getTechnician()));
        if (task.getInspection() != null) {
            response.setInspectionId(task.getInspection().getId());
        }
        return response;
    }
}
