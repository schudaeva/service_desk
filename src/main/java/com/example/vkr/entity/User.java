package com.example.vkr.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "position", length = 255)
    private String position;

    @Column(name = "department", length = 255)
    private String department;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Связь с ролями (многие ко многим)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Заявки, созданные пользователем
    @OneToMany(mappedBy = "createdBy")
    private Set<Request> createdRequests = new HashSet<>();

    // Заявки, назначенные пользователю
    @OneToMany(mappedBy = "assignedTo")
    private Set<Request> assignedRequests = new HashSet<>();

    // Комментарии пользователя
    @OneToMany(mappedBy = "user")
    private Set<Comment> comments = new HashSet<>();

    // История изменений статусов
    @OneToMany(mappedBy = "user")
    private Set<StatusHistory> statusHistories = new HashSet<>();

    // Закрепление за объектами
    @OneToMany(mappedBy = "worker")
    private Set<EquipmentWorker> equipmentWorkers = new HashSet<>();

    // Загруженные вложения
    @OneToMany(mappedBy = "uploadedBy")
    private Set<RequestAttachment> attachments = new HashSet<>();
}