package com.example.vkr.controller;

import com.example.vkr.dto.RequestCreateDto;
import com.example.vkr.entity.*;
import com.example.vkr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dispatcher")
@RequiredArgsConstructor
public class NotificationController {

    private final MaintenanceNotificationRepository notificationRepository;
    private final EquipmentRepository equipmentRepository;
    private final RequestRepository requestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final UserRepository userRepository;

    /**
     * Страница со списком уведомлений
     */
    @GetMapping("/notifications")
    public String showNotifications(Model model) {
        List<MaintenanceNotification> pendingNotifications = notificationRepository
                .findByStatus("PENDING");

        model.addAttribute("notifications", pendingNotifications);
        return "dispatcher/notifications";
    }

    /**
     * Создание заявки из уведомления
     */
    @GetMapping("/notifications/create-request/{id}")
    public String createRequestFromNotification(@PathVariable Long id, Model model) {
        MaintenanceNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        // Предзаполняем DTO
        RequestCreateDto dto = new RequestCreateDto();
        dto.setEquipmentId(notification.getEquipment().getEquipmentId());
        dto.setTitle("Плановое обслуживание: " + notification.getEquipment().getName());
        dto.setDescription("Плановые работы согласно регламенту (периодичность: "
                + notification.getEquipment().getIntervalDays() + " дней)\n" +
                "Рекомендуемая дата: " + notification.getPlannedDate());
        dto.setDeadline(notification.getPlannedDate().atStartOfDay());
        dto.setPriority("MEDIUM");

        model.addAttribute("requestCreateDto", dto);
        model.addAttribute("equipmentList", equipmentRepository.findAll());
        model.addAttribute("workers", userRepository.findAll());
        model.addAttribute("notificationId", id);

        return "dispatcher/create-request-from-notification";
    }

    /**
     * Сохранение заявки из уведомления
     */
    @PostMapping("/notifications/create-request/{id}")
    public String saveRequestFromNotification(@PathVariable Long id,
                                              @ModelAttribute RequestCreateDto dto,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        User creator = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        MaintenanceNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        // Создаём заявку
        Request request = new Request();
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setPriority(dto.getPriority());
        request.setDeadline(dto.getDeadline());
        request.setCreatedBy(creator);
        request.setSource("WEB");

        if (dto.getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Оборудование не найдено"));
            request.setEquipment(equipment);
        }

        if (dto.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Исполнитель не найден"));
            request.setAssignedTo(assignedTo);
            RequestStatus assignedStatus = requestStatusRepository.findByCode("ASSIGNED")
                    .orElseThrow(() -> new RuntimeException("Статус ASSIGNED не найден"));
            request.setStatus(assignedStatus);
        } else {
            RequestStatus newStatus = requestStatusRepository.findByCode("NEW")
                    .orElseThrow(() -> new RuntimeException("Статус NEW не найден"));
            request.setStatus(newStatus);
        }

        Request savedRequest = requestRepository.save(request);

        // Обновляем уведомление
        notification.setStatus("PROCESSED");
        notification.setProcessedAt(LocalDateTime.now());
        notification.setRequest(savedRequest);
        notificationRepository.save(notification);

        return "redirect:/dispatcher/notifications?created=true";
    }
}