package com.MaintHub.demo.repository;

import com.MaintHub.demo.model.DamageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DamageReportRepository extends JpaRepository<DamageReport, Long> {
    List<DamageReport> findByReportedById(Long reportedById);
}
