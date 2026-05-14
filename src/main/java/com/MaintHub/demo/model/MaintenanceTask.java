package com.MaintHub.demo.model;

import com.MaintHub.demo.enums.MaintenanceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "maintenance_tasks")
public class MaintenanceTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status = MaintenanceStatus.ASSIGNED;

    @Column(length = 3000)
    private String repairNotes;

    private BigDecimal estimatedCost;

    private BigDecimal actualCost;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private boolean repairCostApproved = true;

    private LocalDateTime costApprovedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_approved_by_id")
    private User costApprovedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "damage_report_id", nullable = false, unique = true)
    private DamageReport damageReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    @OneToOne(mappedBy = "maintenanceTask")
    private Inspection inspection;

    @JsonIgnore
    @OneToMany(mappedBy = "maintenanceTask", cascade = CascadeType.ALL)
    private List<SparePartRequest> sparePartRequests = new ArrayList<>();
}
