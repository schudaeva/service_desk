package com.example.vkr.service;

import com.example.vkr.entity.Equipment;
import com.example.vkr.entity.MaintenanceNotification;
import com.example.vkr.repository.EquipmentRepository;
import com.example.vkr.repository.MaintenanceNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceScheduler {

    private final EquipmentRepository equipmentRepository;
    private final MaintenanceNotificationRepository notificationRepository;

    /**
     * Запускается каждый день в 8:00 утра
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkPlannedMaintenances() {
        log.info("Запуск планировщика плановых работ...");

        // Получаем все объекты с заданной периодичностью
        List<Equipment> equipmentList = equipmentRepository.findByIntervalDaysNotNull();

        LocalDate today = LocalDate.now();
        int notificationDaysBefore = 3; // за сколько дней уведомлять

        for (Equipment equipment : equipmentList) {
            try {
                processEquipment(equipment, today, notificationDaysBefore);
            } catch (Exception e) {
                log.error("Ошибка при обработке оборудования {}: {}", equipment.getName(), e.getMessage());
            }
        }

        log.info("Планировщик завершил работу");
    }

    private void processEquipment(Equipment equipment, LocalDate today, int daysBefore) {
        // Рассчитываем дату следующей плановой работы
        LocalDate nextMaintenanceDate = calculateNextMaintenanceDate(equipment);

        if (nextMaintenanceDate == null) {
            log.debug("Для оборудования {} не удалось рассчитать дату", equipment.getName());
            return;
        }

        // Проверяем, нужно ли создать уведомление (за daysBefore дней)
        LocalDate notificationDate = nextMaintenanceDate.minusDays(daysBefore);

        if (!notificationDate.equals(today)) {
            return;
        }

        // Проверяем, нет ли уже активного уведомления
        boolean hasPendingNotification = notificationRepository
                .findByEquipmentAndStatus(equipment, "PENDING")
                .isPresent();

        if (hasPendingNotification) {
            log.debug("Для оборудования {} уже есть активное уведомление", equipment.getName());
            return;
        }

        // Создаём новое уведомление
        MaintenanceNotification notification = new MaintenanceNotification();
        notification.setEquipment(equipment);
        notification.setPlannedDate(nextMaintenanceDate);
        notification.setStatus("PENDING");
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);

        log.info("Создано уведомление для оборудования {} на дату {}",
                equipment.getName(), nextMaintenanceDate);
    }

    private LocalDate calculateNextMaintenanceDate(Equipment equipment) {
        // Если есть дата последнего обслуживания
        if (equipment.getLastMaint() != null) {
            return equipment.getLastMaint().plusDays(equipment.getIntervalDays());
        }
        // Если есть дата ввода в эксплуатацию
        if (equipment.getInstallDate() != null) {
            return equipment.getInstallDate().plusDays(equipment.getIntervalDays());
        }
        return null;
    }
}