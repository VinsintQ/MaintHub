package com.MaintHub.demo.dto.response;

import com.MaintHub.demo.enums.InspectionResult;
import com.MaintHub.demo.model.Inspection;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InspectionResponse {
    private Long id;
    private InspectionResult result;
    private String notes;
    private LocalDateTime inspectedAt;
    private LocalDate nextInspectionDate;
    private Long equipmentId;
    private String equipmentName;
    private Long maintenanceTaskId;
    private UserSummaryResponse inspector;

    public static InspectionResponse from(Inspection inspection) {
        InspectionResponse response = new InspectionResponse();
        response.setId(inspection.getId());
        response.setResult(inspection.getResult());
        response.setNotes(inspection.getNotes());
        response.setInspectedAt(inspection.getInspectedAt());
        response.setNextInspectionDate(inspection.getNextInspectionDate());
        response.setEquipmentId(inspection.getEquipment().getId());
        response.setEquipmentName(inspection.getEquipment().getName());
        response.setMaintenanceTaskId(inspection.getMaintenanceTask().getId());
        response.setInspector(UserSummaryResponse.from(inspection.getInspector()));
        return response;
    }
}
