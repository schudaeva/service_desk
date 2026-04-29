package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipment_worker")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @Column(name = "is_default")
    private Boolean isDefault = false;
}