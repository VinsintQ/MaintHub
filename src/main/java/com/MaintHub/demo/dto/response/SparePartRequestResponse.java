package com.MaintHub.demo.dto.response;

import com.MaintHub.demo.enums.SparePartStatus;
import com.MaintHub.demo.model.SparePartRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SparePartRequestResponse {
    private Long id;
    private String partName;
    private Integer quantity;
    private BigDecimal estimatedPrice;
    private SparePartStatus status;
    private LocalDateTime requestedAt;
    private Long maintenanceTaskId;
    private UserSummaryResponse requestedBy;
    private UserSummaryResponse approvedBy;

    public static SparePartRequestResponse from(SparePartRequest request) {
        SparePartRequestResponse response = new SparePartRequestResponse();
        response.setId(request.getId());
        response.setPartName(request.getPartName());
        response.setQuantity(request.getQuantity());
        response.setEstimatedPrice(request.getEstimatedPrice());
        response.setStatus(request.getStatus());
        response.setRequestedAt(request.getRequestedAt());
        response.setMaintenanceTaskId(request.getMaintenanceTask().getId());
        response.setRequestedBy(UserSummaryResponse.from(request.getRequestedBy()));
        response.setApprovedBy(UserSummaryResponse.from(request.getApprovedBy()));
        return response;
    }
}
