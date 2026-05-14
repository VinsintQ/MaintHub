package com.MaintHub.demo.repository;

import com.MaintHub.demo.model.SparePartRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SparePartRequestRepository extends JpaRepository<SparePartRequest, Long> {
    List<SparePartRequest> findByMaintenanceTaskId(Long maintenanceTaskId);
}
