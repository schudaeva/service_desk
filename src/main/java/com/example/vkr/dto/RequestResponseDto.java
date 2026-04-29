package com.example.vkr.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RequestResponseDto {
    private Integer requestId;
    private String title;
    private String description;
    private String statusName;
    private String statusColor;
    private String equipmentName;
    private Integer equipmentId;
    private String createdByUsername;
    private String assignedToUsername;
    private String priority;
    private LocalDateTime deadline;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String rowCssClass;
}