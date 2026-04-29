package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "status", length = 50)
    private String status = "PENDING";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToOne
    @JoinColumn(name = "request_id")
    private Request request;
}