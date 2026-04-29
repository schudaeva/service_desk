package com.example.vkr.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RequestCreateDto {
    private String title;
    private String description;
    private Integer equipmentId;
    private String priority;
    private LocalDateTime deadline;
    private Integer assignedToId;
    private List<Integer> materialIds = new ArrayList<>();
    private List<Integer> plannedQuantities = new ArrayList<>();
}