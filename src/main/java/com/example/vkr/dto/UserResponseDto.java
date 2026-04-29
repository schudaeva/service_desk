package com.example.vkr.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponseDto {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String position;
    private String department;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private Set<String> roles;
}