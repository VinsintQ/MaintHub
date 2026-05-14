package com.MaintHub.demo.service;

import com.MaintHub.demo.dto.response.EquipmentStatusHistoryResponse;
import com.MaintHub.demo.enums.EquipmentStatus;
import com.MaintHub.demo.exception.EquipmentNotFoundException;
import com.MaintHub.demo.exception.InformationNotFoundException;
import com.MaintHub.demo.model.Equipment;
import com.MaintHub.demo.model.EquipmentStatusHistory;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.EquipmentRepository;
import com.MaintHub.demo.repository.EquipmentStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentStatusHistoryService {
    private final EquipmentStatusHistoryRepository historyRepository;
    private final EquipmentRepository equipmentRepository;

    public EquipmentStatusHistoryService(
            EquipmentStatusHistoryRepository historyRepository,
            EquipmentRepository equipmentRepository
    ) {
        this.historyRepository = historyRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional
    public void changeStatus(Equipment equipment, EquipmentStatus newStatus, String reason, User changedBy) {
        EquipmentStatus oldStatus = equipment.getStatus();
        if (oldStatus == newStatus) {
            return;
        }

        equipment.setStatus(newStatus);
        equipmentRepository.save(equipment);

        EquipmentStatusHistory history = new EquipmentStatusHistory();
        history.setEquipment(equipment);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setReason(reason);
        history.setChangedBy(changedBy);
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<EquipmentStatusHistoryResponse> getByEquipment(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new EquipmentNotFoundException("Equipment not found with id " + equipmentId);
        }
        return historyRepository.findByEquipmentIdOrderByChangedAtDesc(equipmentId).stream()
                .map(EquipmentStatusHistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentStatusHistoryResponse> getAll() {
        return historyRepository.findAll().stream()
                .map(EquipmentStatusHistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentStatusHistoryResponse getById(Long id) {
        return historyRepository.findById(id)
                .map(EquipmentStatusHistoryResponse::from)
                .orElseThrow(() -> new InformationNotFoundException("Status history not found with id " + id));
    }
}
