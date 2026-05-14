package com.MaintHub.demo.service;

import com.MaintHub.demo.dto.request.EquipmentCategoryRequest;
import com.MaintHub.demo.dto.response.EquipmentCategoryResponse;
import com.MaintHub.demo.exception.DuplicateCategoryNameException;
import com.MaintHub.demo.exception.EquipmentCategoryNotFoundException;
import com.MaintHub.demo.exception.InvalidWorkflowActionException;
import com.MaintHub.demo.model.EquipmentCategory;
import com.MaintHub.demo.repository.EquipmentCategoryRepository;
import com.MaintHub.demo.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentCategoryService {
    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;

    public EquipmentCategoryService(
            EquipmentCategoryRepository categoryRepository,
            EquipmentRepository equipmentRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional
    public EquipmentCategoryResponse create(EquipmentCategoryRequest request) {
        validateInspectionInterval(request);
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateCategoryNameException("Equipment category already exists with name " + request.getName());
        }

        EquipmentCategory category = new EquipmentCategory();
        applyRequest(category, request);
        return EquipmentCategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<EquipmentCategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(EquipmentCategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentCategoryResponse getById(Long id) {
        return EquipmentCategoryResponse.from(findCategory(id));
    }

    @Transactional
    public EquipmentCategoryResponse update(Long id, EquipmentCategoryRequest request) {
        validateInspectionInterval(request);
        EquipmentCategory category = findCategory(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateCategoryNameException("Equipment category already exists with name " + request.getName());
        }

        applyRequest(category, request);
        return EquipmentCategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        EquipmentCategory category = findCategory(id);
        if (equipmentRepository.existsByCategoryId(id)) {
            throw new InvalidWorkflowActionException("Cannot delete category while equipment is assigned to it");
        }
        categoryRepository.delete(category);
    }

    private EquipmentCategory findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EquipmentCategoryNotFoundException("Equipment category not found with id " + id));
    }

    private void applyRequest(EquipmentCategory category, EquipmentCategoryRequest request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setRequiresRegularInspection(request.isRequiresRegularInspection());
        category.setInspectionIntervalDays(request.getInspectionIntervalDays());
    }

    private void validateInspectionInterval(EquipmentCategoryRequest request) {
        if (request.isRequiresRegularInspection() && request.getInspectionIntervalDays() == null) {
            throw new InvalidWorkflowActionException("Inspection interval days is required for regular inspections");
        }
    }
}
