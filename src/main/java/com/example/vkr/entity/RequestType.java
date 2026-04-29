package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "request_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "default_priority", length = 50)
    private String defaultPriority;

    @Column(name = "default_hours")
    private Integer defaultHours;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "type")
    private Set<Request> requests = new HashSet<>();
}