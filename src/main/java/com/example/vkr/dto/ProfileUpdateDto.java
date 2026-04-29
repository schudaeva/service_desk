package com.example.vkr.dto;

import lombok.Data;

@Data
public class ProfileUpdateDto {
    private String email;
    private String fullName;
    private String phone;
    private String position;
    private String department;
}