package com.example.vkr.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ReportDto {
    // Общая статистика
    private long totalRequests;
    private long completedRequests;
    private long overdueRequests;
    private long inProgressRequests;

    // Статистика по статусам
    private Map<String, Long> requestsByStatus;

    // Статистика по месяцам
    private Map<String, Long> requestsByMonth;

    // Загрузка исполнителей
    private Map<String, Long> workerLoad;

    private LocalDateTime generatedAt;
}