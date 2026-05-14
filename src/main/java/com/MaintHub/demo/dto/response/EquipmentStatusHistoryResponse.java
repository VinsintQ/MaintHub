package com.MaintHub.demo.dto.response;

import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.model.EquipmentStatusHistory;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentStatusHistoryResponse {
    private Long id;
    private EquipmentStatus oldStatus;
    private EquipmentStatus newStatus;
    private String reason;
    private LocalDateTime changedAt;
    private Long equipmentId;
    private String equipmentName;
    private UserSummaryResponse changedBy;

    public static EquipmentStatusHistoryResponse from(EquipmentStatusHistory history) {
        EquipmentStatusHistoryResponse response = new EquipmentStatusHistoryResponse();
        response.setId(history.getId());
        response.setOldStatus(history.getOldStatus());
        response.setNewStatus(history.getNewStatus());
        response.setReason(history.getReason());
        response.setChangedAt(history.getChangedAt());
        response.setEquipmentId(history.getEquipment().getId());
        response.setEquipmentName(history.getEquipment().getName());
        response.setChangedBy(UserSummaryResponse.from(history.getChangedBy()));
        return response;
    }
}
