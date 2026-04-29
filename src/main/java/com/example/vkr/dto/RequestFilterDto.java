package com.example.vkr.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class RequestFilterDto {
    private String search;
    private Integer equipmentId;
    private String statusCode;
    private Integer assignedToId;
    private String priority;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deadlineFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deadlineTo;

    private String sortField = "requestId";
    private String sortDir = "desc";
}