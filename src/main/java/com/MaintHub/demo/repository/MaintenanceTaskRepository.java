package com.MaintHub.demo.repository;

import com.MaintHub.demo.model.MaintenanceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {
    List<MaintenanceTask> findByTechnicianId(Long technicianId);

    boolean existsByDamageReportId(Long damageReportId);

    Optional<MaintenanceTask> findByDamageReportId(Long damageReportId);
}
