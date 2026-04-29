package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "request_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "is_final")
    private Boolean isFinal = false;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "color", length = 7)
    private String color = "#888888";

    @OneToMany(mappedBy = "status")
    private Set<Request> requests = new HashSet<>();

    @OneToMany(mappedBy = "status")
    private Set<StatusHistory> statusHistories = new HashSet<>();

    @OneToMany(mappedBy = "fromStatus")
    private Set<StatusTransition> fromTransitions = new HashSet<>();

    @OneToMany(mappedBy = "toStatus")
    private Set<StatusTransition> toTransitions = new HashSet<>();
}