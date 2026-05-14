package com.MaintHub.demo.controller;

import com.MaintHub.demo.dto.response.EquipmentStatusHistoryResponse;
import com.MaintHub.demo.service.EquipmentStatusHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EquipmentStatusHistoryController {
    private final EquipmentStatusHistoryService statusHistoryService;

    public EquipmentStatusHistoryController(EquipmentStatusHistoryService statusHistoryService) {
        this.statusHistoryService = statusHistoryService;
    }

    @GetMapping("/api/equipment/{equipmentId}/status-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EquipmentStatusHistoryResponse>> getByEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(statusHistoryService.getByEquipment(equipmentId));
    }

    @GetMapping("/api/status-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EquipmentStatusHistoryResponse>> getAll() {
        return ResponseEntity.ok(statusHistoryService.getAll());
    }

    @GetMapping("/api/status-history/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentStatusHistoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(statusHistoryService.getById(id));
    }
}
