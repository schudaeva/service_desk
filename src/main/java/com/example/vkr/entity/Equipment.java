package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Integer equipmentId;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private EquipmentType type;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Column(name = "interval_days")
    private Integer intervalDays;

    @Column(name = "last_maint")
    private LocalDate lastMaint;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "equipment")
    private Set<Request> requests = new HashSet<>();

    @OneToMany(mappedBy = "equipment")
    private Set<EquipmentWorker> equipmentWorkers = new HashSet<>();

    @OneToMany(mappedBy = "equipment")
    private Set<MaintenanceNotification> maintenanceNotifications = new HashSet<>();
}