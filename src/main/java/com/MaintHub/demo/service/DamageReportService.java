package com.MaintHub.demo.service;

import com.MaintHub.demo.dto.request.DamageReportAssignRequest;
import com.MaintHub.demo.dto.request.DamageReportCreateRequest;
import com.MaintHub.demo.dto.request.WorkflowReasonRequest;
import com.MaintHub.demo.dto.response.DamageReportResponse;
import com.MaintHub.demo.enums.DamageReportStatus;
import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.enums.MaintenanceStatus;
import com.MaintHub.demo.exception.DamageReportNotFoundException;
import com.MaintHub.demo.exception.InvalidWorkflowActionException;
import com.MaintHub.demo.exception.UnauthorizedActionException;
import com.MaintHub.demo.exception.UserNotFoundException;
import com.MaintHub.demo.model.DamageReport;
import com.MaintHub.demo.model.Equipment;
import com.MaintHub.demo.model.MaintenanceTask;
import com.MaintHub.demo.model.RoleName;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.DamageReportRepository;
import com.MaintHub.demo.repository.MaintenanceTaskRepository;
import com.MaintHub.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DamageReportService {
    private static final BigDecimal REPAIR_APPROVAL_THRESHOLD = new BigDecimal("100");
    private static final String DAMAGE_REPORT_PHOTO_FOLDER = "mainthub/damage-reports";

    private final DamageReportRepository damageReportRepository;
    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final UserRepository userRepository;
    private final EquipmentService equipmentService;
    private final EquipmentStatusHistoryService statusHistoryService;
    private final CurrentUserService currentUserService;
    private final CloudinaryService cloudinaryService;

    public DamageReportService(
            DamageReportRepository damageReportRepository,
            MaintenanceTaskRepository maintenanceTaskRepository,
            UserRepository userRepository,
            EquipmentService equipmentService,
            EquipmentStatusHistoryService statusHistoryService,
            CurrentUserService currentUserService,
            CloudinaryService cloudinaryService
    ) {
        this.damageReportRepository = damageReportRepository;
        this.maintenanceTaskRepository = maintenanceTaskRepository;
        this.userRepository = userRepository;
        this.equipmentService = equipmentService;
        this.statusHistoryService = statusHistoryService;
        this.currentUserService = currentUserService;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public DamageReportResponse create(DamageReportCreateRequest request) {
        return create(request, null);
    }

    @Transactional
    public DamageReportResponse create(DamageReportCreateRequest request, MultipartFile file) {
        User reporter = currentUserService.getCurrentUser();
        Equipment equipment = equipmentService.findEquipment(request.getEquipmentId());

        DamageReport report = new DamageReport();
        report.setEquipment(equipment);
        report.setReportedBy(reporter);
        report.setDescription(request.getDescription());
        report.setSeverity(request.getSeverity());
        if (file != null && !file.isEmpty()) {
            report.setDamagePhotoUrl(cloudinaryService.uploadImage(file, DAMAGE_REPORT_PHOTO_FOLDER));
        }
        DamageReport savedReport = damageReportRepository.save(report);

        statusHistoryService.changeStatus(
                equipment,
                EquipmentStatus.DAMAGED,
                "Damage report submitted: #" + savedReport.getId(),
                reporter
        );

        return DamageReportResponse.from(savedReport);
    }

    @Transactional(readOnly = true)
    public List<DamageReportResponse> getAll() {
        return damageReportRepository.findAll().stream()
                .map(DamageReportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DamageReportResponse getById(Long id) {
        DamageReport report = findReport(id);
        ensureCanView(report);
        return DamageReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<DamageReportResponse> getMyReports() {
        User reporter = currentUserService.getCurrentUser();
        if (currentUserService.isAdmin(reporter)) {
            return getAll();
        }
        return damageReportRepository.findByReportedById(reporter.getId()).stream()
                .map(DamageReportResponse::from)
                .toList();
    }

    @Transactional
    public DamageReportResponse review(Long id, WorkflowReasonRequest request) {
        DamageReport report = findReport(id);
        if (report.getStatus() != DamageReportStatus.SUBMITTED) {
            throw new InvalidWorkflowActionException("Only submitted reports can be reviewed");
        }
        report.setStatus(DamageReportStatus.REVIEWED);
        return DamageReportResponse.from(damageReportRepository.save(report));
    }

    @Transactional
    public DamageReportResponse reject(Long id, WorkflowReasonRequest request) {
        DamageReport report = findReport(id);
        if (report.getStatus() == DamageReportStatus.ASSIGNED || report.getStatus() == DamageReportStatus.RESOLVED) {
            throw new InvalidWorkflowActionException("Assigned or resolved reports cannot be rejected");
        }
        report.setStatus(DamageReportStatus.REJECTED);
        return DamageReportResponse.from(damageReportRepository.save(report));
    }

    @Transactional
    public DamageReportResponse assignTechnician(Long id, DamageReportAssignRequest request) {
        DamageReport report = findReport(id);
        if (report.getStatus() == DamageReportStatus.REJECTED || report.getStatus() == DamageReportStatus.RESOLVED) {
            throw new InvalidWorkflowActionException("Rejected or resolved reports cannot be assigned");
        }
        if (maintenanceTaskRepository.existsByDamageReportId(id)) {
            throw new InvalidWorkflowActionException("Maintenance task already exists for damage report " + id);
        }

        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new UserNotFoundException("Technician not found with id " + request.getTechnicianId()));
        if (!currentUserService.hasRole(technician, RoleName.ROLE_TECHNICIAN)) {
            throw new InvalidWorkflowActionException("Assigned user must have TECHNICIAN role");
        }

        MaintenanceTask task = new MaintenanceTask();
        task.setDamageReport(report);
        task.setEquipment(report.getEquipment());
        task.setTechnician(technician);
        task.setStatus(MaintenanceStatus.ASSIGNED);
        task.setEstimatedCost(request.getEstimatedCost());
        task.setRepairCostApproved(!requiresRepairApproval(request.getEstimatedCost()));
        if (task.isRepairCostApproved()) {
            task.setCostApprovedAt(LocalDateTime.now());
            task.setCostApprovedBy(currentUserService.getCurrentUser());
        }

        MaintenanceTask savedTask = maintenanceTaskRepository.save(task);
        report.setStatus(DamageReportStatus.ASSIGNED);
        report.setMaintenanceTask(savedTask);
        DamageReport savedReport = damageReportRepository.save(report);

        if (!savedTask.isRepairCostApproved()) {
            statusHistoryService.changeStatus(
                    report.getEquipment(),
                    EquipmentStatus.REPAIR_APPROVAL_REQUIRED,
                    "Repair estimate exceeds 100 BHD for task #" + savedTask.getId(),
                    currentUserService.getCurrentUser()
            );
        }

        return DamageReportResponse.from(savedReport);
    }

    @Transactional
    public DamageReportResponse uploadPhoto(Long id, MultipartFile file) {
        DamageReport report = findReport(id);
        User currentUser = currentUserService.getCurrentUser();
        if (!currentUserService.isAdmin(currentUser) && !report.getReportedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You can only upload photos for your own damage reports");
        }
        String photoUrl = cloudinaryService.uploadImage(file, DAMAGE_REPORT_PHOTO_FOLDER);
        report.setDamagePhotoUrl(photoUrl);
        return DamageReportResponse.from(damageReportRepository.save(report));
    }

    public DamageReport findReport(Long id) {
        return damageReportRepository.findById(id)
                .orElseThrow(() -> new DamageReportNotFoundException("Damage report not found with id " + id));
    }

    private boolean requiresRepairApproval(BigDecimal estimatedCost) {
        return estimatedCost != null && estimatedCost.compareTo(REPAIR_APPROVAL_THRESHOLD) > 0;
    }

    private void ensureCanView(DamageReport report) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (report.getReportedBy().getId().equals(currentUser.getId())) {
            return;
        }
        if (report.getMaintenanceTask() != null
                && report.getMaintenanceTask().getTechnician().getId().equals(currentUser.getId())) {
            return;
        }
        throw new UnauthorizedActionException("You are not allowed to view this damage report");
    }
}
