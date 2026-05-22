package com.MaintHub.demo.controller;

import com.MaintHub.demo.dto.request.InspectionCreateRequest;
import com.MaintHub.demo.dto.request.InspectionDecisionRequest;
import com.MaintHub.demo.dto.response.InspectionResponse;
import com.MaintHub.demo.service.InspectionService;
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
@RequestMapping("/api/inspections")
public class InspectionController {
    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<InspectionResponse> create(@Valid @RequestBody InspectionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inspectionService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<List<InspectionResponse>> getAll() {
        return ResponseEntity.ok(inspectionService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<InspectionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionService.getById(id));
    }

    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR','TECHNICIAN')")
    public ResponseEntity<List<InspectionResponse>> getByEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(inspectionService.getByEquipment(equipmentId));
    }

    @PatchMapping("/{id}/pass")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<InspectionResponse> pass(
            @PathVariable Long id,
            @RequestBody(required = false) InspectionDecisionRequest request
    ) {
        return ResponseEntity.ok(inspectionService.pass(id, request));
    }

    @PatchMapping("/{id}/fail")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<InspectionResponse> fail(
            @PathVariable Long id,
            @RequestBody(required = false) InspectionDecisionRequest request
    ) {
        return ResponseEntity.ok(inspectionService.fail(id, request));
    }

    @PatchMapping("/{id}/mark-unsafe")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<InspectionResponse> markUnsafe(
            @PathVariable Long id,
            @RequestBody(required = false) InspectionDecisionRequest request
    ) {
        return ResponseEntity.ok(inspectionService.markUnsafe(id, request));
    }
}
