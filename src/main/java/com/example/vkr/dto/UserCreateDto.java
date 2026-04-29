package com.example.vkr.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserCreateDto {
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String position;
    private String department;
    private Set<String> roles;
}