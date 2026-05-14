package com.MaintHub.demo.repository;

import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    boolean existsBySerialNumberIgnoreCase(String serialNumber);

    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);

    boolean existsByCategoryId(Long categoryId);

    List<Equipment> findByStatus(EquipmentStatus status);

    List<Equipment> findByCategoryId(Long categoryId);

    List<Equipment> findByWarrantyExpiryDateBetween(LocalDate startDate, LocalDate endDate);
}
