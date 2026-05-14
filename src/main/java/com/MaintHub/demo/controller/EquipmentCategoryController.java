package com.MaintHub.demo.controller;

import com.MaintHub.demo.dto.request.EquipmentCategoryRequest;
import com.MaintHub.demo.dto.response.EquipmentCategoryResponse;
import com.MaintHub.demo.service.EquipmentCategoryService;
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
@RequestMapping("/api/equipment-categories")
public class EquipmentCategoryController {
    private final EquipmentCategoryService equipmentCategoryService;

    public EquipmentCategoryController(EquipmentCategoryService equipmentCategoryService) {
        this.equipmentCategoryService = equipmentCategoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentCategoryResponse> create(@Valid @RequestBody EquipmentCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentCategoryService.create(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentCategoryResponse>> getAll() {
        return ResponseEntity.ok(equipmentCategoryService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EquipmentCategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentCategoryService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentCategoryRequest request
    ) {
        return ResponseEntity.ok(equipmentCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
