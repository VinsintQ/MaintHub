package com.MaintHub.demo.controller;

import com.MaintHub.demo.dto.request.MaintenanceTaskCompleteRequest;
import com.MaintHub.demo.dto.request.WorkflowReasonRequest;
import com.MaintHub.demo.dto.response.MaintenanceTaskResponse;
import com.MaintHub.demo.service.MaintenanceTaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-tasks")
public class MaintenanceTaskController {
    private final MaintenanceTaskService maintenanceTaskService;

    public MaintenanceTaskController(MaintenanceTaskService maintenanceTaskService) {
        this.maintenanceTaskService = maintenanceTaskService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MaintenanceTaskResponse>> getAll() {
        return ResponseEntity.ok(maintenanceTaskService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN','INSPECTOR')")
    public ResponseEntity<MaintenanceTaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceTaskService.getById(id));
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<List<MaintenanceTaskResponse>> getMyTasks() {
        return ResponseEntity.ok(maintenanceTaskService.getMyTasks());
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<MaintenanceTaskResponse> start(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceTaskService.start(id));
    }

    @PatchMapping("/{id}/waiting-for-parts")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<MaintenanceTaskResponse> markWaitingForParts(
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowReasonRequest request
    ) {
        return ResponseEntity.ok(maintenanceTaskService.markWaitingForParts(id, request));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<MaintenanceTaskResponse> complete(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceTaskCompleteRequest request
    ) {
        return ResponseEntity.ok(maintenanceTaskService.complete(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public ResponseEntity<MaintenanceTaskResponse> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowReasonRequest request
    ) {
        return ResponseEntity.ok(maintenanceTaskService.cancel(id, request));
    }

    @PatchMapping("/{id}/approve-cost")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaintenanceTaskResponse> approveRepairCost(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceTaskService.approveRepairCost(id));
    }
}
