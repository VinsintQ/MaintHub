package com.MaintHub.demo.repository;

import com.MaintHub.demo.model.EquipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentStatusHistoryRepository extends JpaRepository<EquipmentStatusHistory, Long> {
    List<EquipmentStatusHistory> findByEquipmentIdOrderByChangedAtDesc(Long equipmentId);
}
