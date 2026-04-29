package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "planned_quantity", nullable = false)
    private Integer plannedQuantity = 0;

    @Column(name = "actual_quantity")
    private Integer actualQuantity = 0;
}