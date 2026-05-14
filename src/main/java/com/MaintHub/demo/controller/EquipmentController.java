package com.MaintHub.demo.controller;

import com.MaintHub.demo.dto.request.EquipmentRequest;
import com.MaintHub.demo.dto.response.EquipmentResponse;
import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','TECHNICIAN','INSPECTOR','USER')")
    public ResponseEntity<List<EquipmentResponse>> getAll() {
        return ResponseEntity.ok(equipmentService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','TECHNICIAN','INSPECTOR','USER')")
    public ResponseEntity<EquipmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request
    ) {
        return ResponseEntity.ok(equipmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        equipmentService.retire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','TECHNICIAN','INSPECTOR','USER')")
    public ResponseEntity<List<EquipmentResponse>> getByStatus(@PathVariable EquipmentStatus status) {
        return ResponseEntity.ok(equipmentService.getByStatus(status));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','TECHNICIAN','INSPECTOR','USER')")
    public ResponseEntity<List<EquipmentResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(equipmentService.getByCategory(categoryId));
    }

    @GetMapping("/due-inspection")
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    public ResponseEntity<List<EquipmentResponse>> getDueForInspection() {
        return ResponseEntity.ok(equipmentService.getDueForInspection());
    }

    @GetMapping("/warranty-expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EquipmentResponse>> getWarrantyExpiring() {
        return ResponseEntity.ok(equipmentService.getWarrantyExpiring());
    }
}
