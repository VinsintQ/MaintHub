package com.MaintHub.demo.service;

import com.MaintHub.demo.dto.request.InspectionCreateRequest;
import com.MaintHub.demo.dto.request.InspectionDecisionRequest;
import com.MaintHub.demo.dto.response.InspectionResponse;
import com.MaintHub.demo.enums.DamageReportStatus;
import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.enums.InspectionResult;
import com.MaintHub.demo.enums.MaintenanceStatus;
import com.MaintHub.demo.exception.EquipmentNotFoundException;
import com.MaintHub.demo.exception.InspectionNotFoundException;
import com.MaintHub.demo.exception.InvalidWorkflowActionException;
import com.MaintHub.demo.model.Equipment;
import com.MaintHub.demo.model.EquipmentCategory;
import com.MaintHub.demo.model.Inspection;
import com.MaintHub.demo.model.MaintenanceTask;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.EquipmentRepository;
import com.MaintHub.demo.repository.InspectionRepository;
import com.MaintHub.demo.repository.MaintenanceTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InspectionService {
    private final InspectionRepository inspectionRepository;
    private final MaintenanceTaskService maintenanceTaskService;
    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusHistoryService statusHistoryService;
    private final CurrentUserService currentUserService;

    public InspectionService(
            InspectionRepository inspectionRepository,
            MaintenanceTaskService maintenanceTaskService,
            MaintenanceTaskRepository maintenanceTaskRepository,
            EquipmentRepository equipmentRepository,
            EquipmentStatusHistoryService statusHistoryService,
            CurrentUserService currentUserService
    ) {
        this.inspectionRepository = inspectionRepository;
        this.maintenanceTaskService = maintenanceTaskService;
        this.maintenanceTaskRepository = maintenanceTaskRepository;
        this.equipmentRepository = equipmentRepository;
        this.statusHistoryService = statusHistoryService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public InspectionResponse create(InspectionCreateRequest request) {
        MaintenanceTask task = maintenanceTaskService.findTask(request.getMaintenanceTaskId());
        if (task.getStatus() != MaintenanceStatus.COMPLETED) {
            throw new InvalidWorkflowActionException("Inspection can only happen after maintenance is completed");
        }
        if (inspectionRepository.existsByMaintenanceTaskId(task.getId())) {
            throw new InvalidWorkflowActionException("Inspection already exists for maintenance task " + task.getId());
        }

        Inspection inspection = new Inspection();
        inspection.setMaintenanceTask(task);
        inspection.setEquipment(task.getEquipment());
        inspection.setInspector(currentUserService.getCurrentUser());
        inspection.setResult(request.getResult());
        inspection.setNotes(request.getNotes());
        inspection.setInspectedAt(LocalDateTime.now());
        inspection.setNextInspectionDate(calculateNextInspectionDate(task.getEquipment()));

        Inspection savedInspection = inspectionRepository.save(inspection);
        applyInspectionResult(savedInspection);
        return InspectionResponse.from(savedInspection);
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> getAll() {
        return inspectionRepository.findAll().stream()
                .map(InspectionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InspectionResponse getById(Long id) {
        return InspectionResponse.from(findInspection(id));
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> getByEquipment(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new EquipmentNotFoundException("Equipment not found with id " + equipmentId);
        }
        return inspectionRepository.findByEquipmentId(equipmentId).stream()
                .map(InspectionResponse::from)
                .toList();
    }

    @Transactional
    public InspectionResponse pass(Long id, InspectionDecisionRequest request) {
        return updateResult(id, InspectionResult.PASSED, request);
    }

    @Transactional
    public InspectionResponse fail(Long id, InspectionDecisionRequest request) {
        return updateResult(id, InspectionResult.FAILED, request);
    }

    @Transactional
    public InspectionResponse markUnsafe(Long id, InspectionDecisionRequest request) {
        return updateResult(id, InspectionResult.UNSAFE, request);
    }

    private InspectionResponse updateResult(Long id, InspectionResult result, InspectionDecisionRequest request) {
        Inspection inspection = findInspection(id);
        inspection.setResult(result);
        if (request != null && request.getNotes() != null) {
            inspection.setNotes(request.getNotes());
        }
        inspection.setInspectedAt(LocalDateTime.now());
        inspection.setNextInspectionDate(calculateNextInspectionDate(inspection.getEquipment()));
        Inspection savedInspection = inspectionRepository.save(inspection);
        applyInspectionResult(savedInspection);
        return InspectionResponse.from(savedInspection);
    }

    private void applyInspectionResult(Inspection inspection) {
        User inspector = currentUserService.getCurrentUser();
        MaintenanceTask task = inspection.getMaintenanceTask();
        Equipment equipment = inspection.getEquipment();

        if (inspection.getResult() == InspectionResult.PASSED) {
            task.setStatus(MaintenanceStatus.COMPLETED);
            task.getDamageReport().setStatus(DamageReportStatus.RESOLVED);
            maintenanceTaskRepository.save(task);
            statusHistoryService.changeStatus(equipment, EquipmentStatus.ACTIVE, "Inspection passed: #" + inspection.getId(), inspector);
            return;
        }

        if (inspection.getResult() == InspectionResult.UNSAFE) {
            statusHistoryService.changeStatus(equipment, EquipmentStatus.UNSAFE, "Inspection marked unsafe: #" + inspection.getId(), inspector);
            return;
        }

        task.setStatus(MaintenanceStatus.IN_PROGRESS);
        maintenanceTaskRepository.save(task);
        statusHistoryService.changeStatus(
                equipment,
                EquipmentStatus.UNDER_MAINTENANCE,
                "Inspection failed and needs more repair: #" + inspection.getId(),
                inspector
        );
    }

    private LocalDate calculateNextInspectionDate(Equipment equipment) {
        EquipmentCategory category = equipment.getCategory();
        if (!category.isRequiresRegularInspection() || category.getInspectionIntervalDays() == null) {
            return null;
        }
        return LocalDate.now().plusDays(category.getInspectionIntervalDays());
    }

    private Inspection findInspection(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new InspectionNotFoundException("Inspection not found with id " + id));
    }
}
