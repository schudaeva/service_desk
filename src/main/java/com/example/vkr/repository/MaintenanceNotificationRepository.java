package com.example.vkr.repository;

import com.example.vkr.entity.Equipment;
import com.example.vkr.entity.MaintenanceNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceNotificationRepository extends JpaRepository<MaintenanceNotification, Long> {
    List<MaintenanceNotification> findByStatus(String status);
    Optional<MaintenanceNotification> findByEquipmentAndStatus(Equipment equipment, String status);
    List<MaintenanceNotification> findByPlannedDateBeforeAndStatus(LocalDate date, String status);
}