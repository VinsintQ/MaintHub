package com.MaintHub.demo.service;

import com.MaintHub.demo.dto.request.MaintenanceTaskCompleteRequest;
import com.MaintHub.demo.dto.request.WorkflowReasonRequest;
import com.MaintHub.demo.dto.response.MaintenanceTaskResponse;
import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.enums.MaintenanceStatus;
import com.MaintHub.demo.exception.InvalidWorkflowActionException;
import com.MaintHub.demo.exception.MaintenanceTaskNotFoundException;
import com.MaintHub.demo.exception.UnauthorizedActionException;
import com.MaintHub.demo.model.MaintenanceTask;
import com.MaintHub.demo.model.RoleName;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.MaintenanceTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceTaskService {
    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final EquipmentStatusHistoryService statusHistoryService;
    private final CurrentUserService currentUserService;

    public MaintenanceTaskService(
            MaintenanceTaskRepository maintenanceTaskRepository,
            EquipmentStatusHistoryService statusHistoryService,
            CurrentUserService currentUserService
    ) {
        this.maintenanceTaskRepository = maintenanceTaskRepository;
        this.statusHistoryService = statusHistoryService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponse> getAll() {
        return maintenanceTaskRepository.findAll().stream()
                .map(MaintenanceTaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceTaskResponse getById(Long id) {
        MaintenanceTask task = findTask(id);
        ensureCanView(task);
        return MaintenanceTaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTaskResponse> getMyTasks() {
        User technician = currentUserService.getCurrentUser();
        return maintenanceTaskRepository.findByTechnicianId(technician.getId()).stream()
                .map(MaintenanceTaskResponse::from)
                .toList();
    }

    @Transactional
    public MaintenanceTaskResponse start(Long id) {
        MaintenanceTask task = findTask(id);
        ensureAssignedTechnician(task);
        if (task.getStatus() != MaintenanceStatus.ASSIGNED) {
            throw new InvalidWorkflowActionException("Only assigned tasks can be started");
        }
        if (!task.isRepairCostApproved()) {
            throw new InvalidWorkflowActionException("Repair estimate exceeds 100 BHD and requires admin approval");
        }

        task.setStatus(MaintenanceStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());
        MaintenanceTask savedTask = maintenanceTaskRepository.save(task);
        statusHistoryService.changeStatus(
                task.getEquipment(),
                EquipmentStatus.UNDER_MAINTENANCE,
                "Maintenance task started: #" + task.getId(),
                currentUserService.getCurrentUser()
        );
        return MaintenanceTaskResponse.from(savedTask);
    }

    @Transactional
    public MaintenanceTaskResponse markWaitingForParts(Long id, WorkflowReasonRequest request) {
        MaintenanceTask task = findTask(id);
        ensureAssignedTechnician(task);
        if (task.getStatus() != MaintenanceStatus.IN_PROGRESS) {
            throw new InvalidWorkflowActionException("Only in-progress tasks can wait for parts");
        }

        task.setStatus(MaintenanceStatus.WAITING_FOR_PARTS);
        return MaintenanceTaskResponse.from(maintenanceTaskRepository.save(task));
    }

    @Transactional
    public MaintenanceTaskResponse complete(Long id, MaintenanceTaskCompleteRequest request) {
        MaintenanceTask task = findTask(id);
        ensureAssignedTechnician(task);
        if (task.getStatus() != MaintenanceStatus.IN_PROGRESS
                && task.getStatus() != MaintenanceStatus.WAITING_FOR_PARTS) {
            throw new InvalidWorkflowActionException("Only active maintenance tasks can be completed");
        }

        task.setStatus(MaintenanceStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setRepairNotes(request.getRepairNotes());
        task.setActualCost(request.getActualCost());
        MaintenanceTask savedTask = maintenanceTaskRepository.save(task);

        statusHistoryService.changeStatus(
                task.getEquipment(),
                EquipmentStatus.PENDING_INSPECTION,
                "Maintenance task completed: #" + task.getId(),
                currentUserService.getCurrentUser()
        );
        return MaintenanceTaskResponse.from(savedTask);
    }

    @Transactional
    public MaintenanceTaskResponse cancel(Long id, WorkflowReasonRequest request) {
        MaintenanceTask task = findTask(id);
        ensureAdminOrAssignedTechnician(task);
        if (task.getStatus() == MaintenanceStatus.COMPLETED) {
            throw new InvalidWorkflowActionException("Completed maintenance tasks cannot be cancelled");
        }
        if (task.getStatus() == MaintenanceStatus.CANCELLED) {
            throw new InvalidWorkflowActionException("Maintenance task is already cancelled");
        }

        task.setStatus(MaintenanceStatus.CANCELLED);
        MaintenanceTask savedTask = maintenanceTaskRepository.save(task);
        statusHistoryService.changeStatus(
                task.getEquipment(),
                EquipmentStatus.DAMAGED,
                request != null && request.getReason() != null ? request.getReason() : "Maintenance task cancelled: #" + task.getId(),
                currentUserService.getCurrentUser()
        );
        return MaintenanceTaskResponse.from(savedTask);
    }

    @Transactional
    public MaintenanceTaskResponse approveRepairCost(Long id) {
        MaintenanceTask task = findTask(id);
        User admin = currentUserService.getCurrentUser();
        task.setRepairCostApproved(true);
        task.setCostApprovedAt(LocalDateTime.now());
        task.setCostApprovedBy(admin);
        MaintenanceTask savedTask = maintenanceTaskRepository.save(task);

        statusHistoryService.changeStatus(
                task.getEquipment(),
                EquipmentStatus.DAMAGED,
                "Repair cost approved for task #" + task.getId(),
                admin
        );
        return MaintenanceTaskResponse.from(savedTask);
    }

    public MaintenanceTask findTask(Long id) {
        return maintenanceTaskRepository.findById(id)
                .orElseThrow(() -> new MaintenanceTaskNotFoundException("Maintenance task not found with id " + id));
    }

    private void ensureCanView(MaintenanceTask task) {
        User user = currentUserService.getCurrentUser();
        if (currentUserService.isAdmin(user)
                || task.getTechnician().getId().equals(user.getId())
                || currentUserService.hasRole(user, RoleName.ROLE_INSPECTOR)) {
            return;
        }
        throw new UnauthorizedActionException("You are not allowed to view this maintenance task");
    }

    private void ensureAssignedTechnician(MaintenanceTask task) {
        User user = currentUserService.getCurrentUser();
        if (!task.getTechnician().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("Only the assigned technician can update this maintenance task");
        }
    }

    private void ensureAdminOrAssignedTechnician(MaintenanceTask task) {
        User user = currentUserService.getCurrentUser();
        if (currentUserService.isAdmin(user) || task.getTechnician().getId().equals(user.getId())) {
            return;
        }
        throw new UnauthorizedActionException("Only admin or the assigned technician can cancel this maintenance task");
    }
}
