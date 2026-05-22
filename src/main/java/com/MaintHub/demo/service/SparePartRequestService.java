package com.MaintHub.demo.service;

import com.MaintHub.demo.dto.request.SparePartRequestCreateRequest;
import com.MaintHub.demo.dto.response.SparePartRequestResponse;
import com.MaintHub.demo.enums.MaintenanceStatus;
import com.MaintHub.demo.enums.SparePartStatus;
import com.MaintHub.demo.exception.InvalidWorkflowActionException;
import com.MaintHub.demo.exception.SparePartRequestNotFoundException;
import com.MaintHub.demo.exception.UnauthorizedActionException;
import com.MaintHub.demo.model.MaintenanceTask;
import com.MaintHub.demo.model.SparePartRequest;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.SparePartRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SparePartRequestService {
    private final SparePartRequestRepository sparePartRequestRepository;
    private final MaintenanceTaskService maintenanceTaskService;
    private final CurrentUserService currentUserService;

    public SparePartRequestService(
            SparePartRequestRepository sparePartRequestRepository,
            MaintenanceTaskService maintenanceTaskService,
            CurrentUserService currentUserService
    ) {
        this.sparePartRequestRepository = sparePartRequestRepository;
        this.maintenanceTaskService = maintenanceTaskService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public SparePartRequestResponse create(SparePartRequestCreateRequest request) {
        MaintenanceTask task = maintenanceTaskService.findTask(request.getMaintenanceTaskId());
        ensureAssignedTechnician(task);
        if (task.getStatus() != MaintenanceStatus.IN_PROGRESS
                && task.getStatus() != MaintenanceStatus.WAITING_FOR_PARTS) {
            throw new InvalidWorkflowActionException("Spare parts can only be requested during active maintenance");
        }

        SparePartRequest sparePartRequest = new SparePartRequest();
        sparePartRequest.setMaintenanceTask(task);
        sparePartRequest.setRequestedBy(currentUserService.getCurrentUser());
        sparePartRequest.setPartName(request.getPartName());
        sparePartRequest.setQuantity(request.getQuantity());
        sparePartRequest.setEstimatedPrice(request.getEstimatedPrice());
        sparePartRequest.setStatus(SparePartStatus.PENDING);
        return SparePartRequestResponse.from(sparePartRequestRepository.save(sparePartRequest));
    }

    @Transactional(readOnly = true)
    public List<SparePartRequestResponse> getAll() {
        return sparePartRequestRepository.findAll().stream()
                .map(SparePartRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SparePartRequestResponse getById(Long id) {
        SparePartRequest request = findRequest(id);
        ensureCanView(request);
        return SparePartRequestResponse.from(request);
    }

    @Transactional(readOnly = true)
    public List<SparePartRequestResponse> getByMaintenanceTask(Long taskId) {
        MaintenanceTask task = maintenanceTaskService.findTask(taskId);
        User currentUser = currentUserService.getCurrentUser();
        if (!currentUserService.isAdmin(currentUser) && !task.getTechnician().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not allowed to view spare part requests for this task");
        }
        return sparePartRequestRepository.findByMaintenanceTaskId(taskId).stream()
                .map(SparePartRequestResponse::from)
                .toList();
    }

    @Transactional
    public SparePartRequestResponse approve(Long id) {
        SparePartRequest request = findRequest(id);
        ensureStatus(request, SparePartStatus.PENDING, "Only pending spare part requests can be approved");
        request.setStatus(SparePartStatus.APPROVED);
        request.setApprovedBy(currentUserService.getCurrentUser());
        return SparePartRequestResponse.from(sparePartRequestRepository.save(request));
    }

    @Transactional
    public SparePartRequestResponse reject(Long id) {
        SparePartRequest request = findRequest(id);
        ensureStatus(request, SparePartStatus.PENDING, "Only pending spare part requests can be rejected");
        request.setStatus(SparePartStatus.REJECTED);
        request.setApprovedBy(currentUserService.getCurrentUser());
        return SparePartRequestResponse.from(sparePartRequestRepository.save(request));
    }

    @Transactional
    public SparePartRequestResponse markOrdered(Long id) {
        SparePartRequest request = findRequest(id);
        ensureStatus(request, SparePartStatus.APPROVED, "Only approved spare part requests can be marked ordered");
        request.setStatus(SparePartStatus.ORDERED);
        return SparePartRequestResponse.from(sparePartRequestRepository.save(request));
    }

    @Transactional
    public SparePartRequestResponse markReceived(Long id) {
        SparePartRequest request = findRequest(id);
        ensureStatus(request, SparePartStatus.ORDERED, "Only ordered spare part requests can be marked received");
        request.setStatus(SparePartStatus.RECEIVED);
        return SparePartRequestResponse.from(sparePartRequestRepository.save(request));
    }

    private SparePartRequest findRequest(Long id) {
        return sparePartRequestRepository.findById(id)
                .orElseThrow(() -> new SparePartRequestNotFoundException("Spare part request not found with id " + id));
    }

    private void ensureAssignedTechnician(MaintenanceTask task) {
        User currentUser = currentUserService.getCurrentUser();
        if (!currentUserService.isAdmin(currentUser) && !task.getTechnician().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the assigned technician can request spare parts for this task");
        }
    }

    private void ensureCanView(SparePartRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUserService.isAdmin(currentUser)
                || request.getRequestedBy().getId().equals(currentUser.getId())
                || request.getMaintenanceTask().getTechnician().getId().equals(currentUser.getId())) {
            return;
        }
        throw new UnauthorizedActionException("You are not allowed to view this spare part request");
    }

    private void ensureStatus(SparePartRequest request, SparePartStatus expectedStatus, String message) {
        if (request.getStatus() != expectedStatus) {
            throw new InvalidWorkflowActionException(message);
        }
    }
}
