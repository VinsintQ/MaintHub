package com.MaintHub.demo.service;

import com.MaintHub.demo.dto.request.EquipmentRequest;
import com.MaintHub.demo.dto.response.EquipmentResponse;
import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.exception.DuplicateSerialNumberException;
import com.MaintHub.demo.exception.EquipmentCategoryNotFoundException;
import com.MaintHub.demo.exception.EquipmentNotFoundException;
import com.MaintHub.demo.model.Equipment;
import com.MaintHub.demo.model.EquipmentCategory;
import com.MaintHub.demo.model.Inspection;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.EquipmentCategoryRepository;
import com.MaintHub.demo.repository.EquipmentRepository;
import com.MaintHub.demo.repository.InspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final InspectionRepository inspectionRepository;
    private final EquipmentStatusHistoryService statusHistoryService;
    private final CurrentUserService currentUserService;

    public EquipmentService(
            EquipmentRepository equipmentRepository,
            EquipmentCategoryRepository categoryRepository,
            InspectionRepository inspectionRepository,
            EquipmentStatusHistoryService statusHistoryService,
            CurrentUserService currentUserService
    ) {
        this.equipmentRepository = equipmentRepository;
        this.categoryRepository = categoryRepository;
        this.inspectionRepository = inspectionRepository;
        this.statusHistoryService = statusHistoryService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public EquipmentResponse create(EquipmentRequest request) {
        if (equipmentRepository.existsBySerialNumberIgnoreCase(request.getSerialNumber())) {
            throw new DuplicateSerialNumberException("Equipment already exists with serial number " + request.getSerialNumber());
        }

        Equipment equipment = new Equipment();
        applyRequest(equipment, request, true);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        if (request.getStatus() != null && request.getStatus() != EquipmentStatus.ACTIVE) {
            statusHistoryService.changeStatus(
                    savedEquipment,
                    request.getStatus(),
                    "Initial equipment status set by admin",
                    currentUserService.getCurrentUser()
            );
        }

        return EquipmentResponse.from(savedEquipment);
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAll() {
        return equipmentRepository.findAll().stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getById(Long id) {
        return EquipmentResponse.from(findEquipment(id));
    }

    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = findEquipment(id);
        if (equipmentRepository.existsBySerialNumberIgnoreCaseAndIdNot(request.getSerialNumber(), id)) {
            throw new DuplicateSerialNumberException("Equipment already exists with serial number " + request.getSerialNumber());
        }

        EquipmentStatus requestedStatus = request.getStatus();
        applyRequest(equipment, request, true);
        equipmentRepository.save(equipment);

        if (requestedStatus != null) {
            User changedBy = currentUserService.getCurrentUser();
            statusHistoryService.changeStatus(equipment, requestedStatus, "Equipment status updated by admin", changedBy);
        }

        return EquipmentResponse.from(equipment);
    }

    @Transactional
    public void retire(Long id) {
        Equipment equipment = findEquipment(id);
        statusHistoryService.changeStatus(
                equipment,
                EquipmentStatus.RETIRED,
                "Equipment retired instead of physical deletion",
                currentUserService.getCurrentUser()
        );
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getByStatus(EquipmentStatus status) {
        return equipmentRepository.findByStatus(status).stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EquipmentCategoryNotFoundException("Equipment category not found with id " + categoryId);
        }
        return equipmentRepository.findByCategoryId(categoryId).stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getDueForInspection() {
        LocalDate today = LocalDate.now();
        return equipmentRepository.findAll().stream()
                .filter(this::requiresRegularInspection)
                .filter(equipment -> isDueForInspection(equipment, today))
                .map(EquipmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getWarrantyExpiring() {
        LocalDate today = LocalDate.now();
        return equipmentRepository.findByWarrantyExpiryDateBetween(today, today.plusDays(30)).stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    public Equipment findEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException("Equipment not found with id " + id));
    }

    private void applyRequest(Equipment equipment, EquipmentRequest request, boolean keepExistingStatus) {
        EquipmentCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EquipmentCategoryNotFoundException("Equipment category not found with id " + request.getCategoryId()));

        equipment.setName(request.getName());
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setDescription(request.getDescription());
        equipment.setLocation(request.getLocation());
        equipment.setConditionLevel(request.getConditionLevel());
        equipment.setPurchaseDate(request.getPurchaseDate());
        equipment.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        equipment.setCategory(category);

        if (!keepExistingStatus && request.getStatus() != null) {
            equipment.setStatus(request.getStatus());
        }
    }

    private boolean requiresRegularInspection(Equipment equipment) {
        EquipmentCategory category = equipment.getCategory();
        return category.isRequiresRegularInspection() && category.getInspectionIntervalDays() != null;
    }

    private boolean isDueForInspection(Equipment equipment, LocalDate today) {
        return inspectionRepository.findTopByEquipmentIdOrderByInspectedAtDesc(equipment.getId())
                .map(Inspection::getNextInspectionDate)
                .map(nextInspectionDate -> !nextInspectionDate.isAfter(today))
                .orElse(true);
    }
}
