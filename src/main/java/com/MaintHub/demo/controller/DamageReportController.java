package com.MaintHub.demo.controller;

import com.MaintHub.demo.dto.request.DamagePhotoUploadRequest;
import com.MaintHub.demo.dto.request.DamageReportAssignRequest;
import com.MaintHub.demo.dto.request.DamageReportCreateRequest;
import com.MaintHub.demo.dto.request.WorkflowReasonRequest;
import com.MaintHub.demo.dto.response.DamageReportResponse;
import com.MaintHub.demo.service.DamageReportService;
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
@RequestMapping("/api/damage-reports")
public class DamageReportController {
    private final DamageReportService damageReportService;

    public DamageReportController(DamageReportService damageReportService) {
        this.damageReportService = damageReportService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','USER','ADMIN')")
    public ResponseEntity<DamageReportResponse> create(@Valid @RequestBody DamageReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(damageReportService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DamageReportResponse>> getAll() {
        return ResponseEntity.ok(damageReportService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','TECHNICIAN','USER')")
    public ResponseEntity<DamageReportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(damageReportService.getById(id));
    }

    @GetMapping("/my-reports")
    @PreAuthorize("hasAnyRole('STAFF','USER','ADMIN')")
    public ResponseEntity<List<DamageReportResponse>> getMyReports() {
        return ResponseEntity.ok(damageReportService.getMyReports());
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DamageReportResponse> review(
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowReasonRequest request
    ) {
        return ResponseEntity.ok(damageReportService.review(id, request));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DamageReportResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowReasonRequest request
    ) {
        return ResponseEntity.ok(damageReportService.reject(id, request));
    }

    @PatchMapping("/{id}/assign-technician")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DamageReportResponse> assignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody DamageReportAssignRequest request
    ) {
        return ResponseEntity.ok(damageReportService.assignTechnician(id, request));
    }

    @PostMapping("/{id}/upload-photo")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    public ResponseEntity<DamageReportResponse> uploadPhoto(
            @PathVariable Long id,
            @Valid @RequestBody DamagePhotoUploadRequest request
    ) {
        return ResponseEntity.ok(damageReportService.uploadPhoto(id, request));
    }
}
