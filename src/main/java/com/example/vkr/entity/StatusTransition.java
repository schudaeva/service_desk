package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status_transition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_status", nullable = false)
    private RequestStatus fromStatus;

    @ManyToOne
    @JoinColumn(name = "to_status", nullable = false)
    private RequestStatus toStatus;

    @Column(name = "role_id", nullable = false, length = 50)
    private String roleId; // ADMIN, DISPATCHER, WORKER, REQUESTER
}