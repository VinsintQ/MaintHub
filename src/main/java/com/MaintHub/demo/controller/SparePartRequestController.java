package com.MaintHub.demo.controller;

import com.MaintHub.demo.dto.request.SparePartRequestCreateRequest;
import com.MaintHub.demo.dto.response.SparePartRequestResponse;
import com.MaintHub.demo.service.SparePartRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/spare-part-requests")
public class SparePartRequestController {
    private final SparePartRequestService sparePartRequestService;

    public SparePartRequestController(SparePartRequestService sparePartRequestService) {
        this.sparePartRequestService = sparePartRequestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<SparePartRequestResponse> create(@Valid @RequestBody SparePartRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sparePartRequestService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SparePartRequestResponse>> getAll() {
        return ResponseEntity.ok(sparePartRequestService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public ResponseEntity<SparePartRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sparePartRequestService.getById(id));
    }

    @GetMapping("/maintenance-task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    public ResponseEntity<List<SparePartRequestResponse>> getByMaintenanceTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(sparePartRequestService.getByMaintenanceTask(taskId));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SparePartRequestResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(sparePartRequestService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SparePartRequestResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(sparePartRequestService.reject(id));
    }

    @PatchMapping("/{id}/mark-ordered")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SparePartRequestResponse> markOrdered(@PathVariable Long id) {
        return ResponseEntity.ok(sparePartRequestService.markOrdered(id));
    }

    @PatchMapping("/{id}/mark-received")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SparePartRequestResponse> markReceived(@PathVariable Long id) {
        return ResponseEntity.ok(sparePartRequestService.markReceived(id));
    }
}
