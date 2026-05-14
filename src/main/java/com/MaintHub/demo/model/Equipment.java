package com.MaintHub.demo.model;

import com.MaintHub.demo.enums.EquipmentStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(length = 2000)
    private String description;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status = EquipmentStatus.ACTIVE;

    private Integer conditionLevel;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private EquipmentCategory category;

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL)
    private List<DamageReport> damageReports = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL)
    private List<MaintenanceTask> maintenanceTasks = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL)
    private List<Inspection> inspections = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL)
    private List<EquipmentStatusHistory> statusHistory = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EquipmentStatus.ACTIVE;
        }
    }
}
