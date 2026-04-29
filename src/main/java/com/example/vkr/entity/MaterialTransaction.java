package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "material_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaterialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // INCOME, EXPENSE, RETURN

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}