package com.example.vkr.service;

import com.example.vkr.dto.ReportDto;
import com.example.vkr.entity.Request;
import com.example.vkr.entity.User;
import com.example.vkr.repository.RequestRepository;
import com.example.vkr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    /**
     * Сбор статистики для отчёта
     */
    public ReportDto collectStatistics() {
        List<Request> allRequests = requestRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        ReportDto report = new ReportDto();
        report.setGeneratedAt(now);
        report.setTotalRequests(allRequests.size());

        // Статистика по статусам
        Map<String, Long> byStatus = allRequests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus() != null ? r.getStatus().getName() : "Неизвестно",
                        Collectors.counting()
                ));
        report.setRequestsByStatus(byStatus);

        // Количество выполненных
        long completed = allRequests.stream()
                .filter(r -> r.getStatus() != null && "COMPLETED".equals(r.getStatus().getCode()))
                .count();
        report.setCompletedRequests(completed);

        // Количество просроченных
        long overdue = allRequests.stream()
                .filter(r -> r.getDeadline() != null && r.getDeadline().isBefore(now)
                        && !(r.getStatus() != null && "COMPLETED".equals(r.getStatus().getCode())))
                .count();
        report.setOverdueRequests(overdue);

        // Количество в работе
        long inProgress = allRequests.stream()
                .filter(r -> r.getStatus() != null && "IN_PROGRESS".equals(r.getStatus().getCode()))
                .count();
        report.setInProgressRequests(inProgress);

        // Статистика по месяцам
        Map<String, Long> byMonth = allRequests.stream()
                .filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        Collectors.counting()
                ));
        report.setRequestsByMonth(byMonth);

        // Загрузка исполнителей (только WORKER)
        List<User> workers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "WORKER".equals(r.getName())))
                .collect(Collectors.toList());

        Map<String, Long> workerLoad = new LinkedHashMap<>();
        for (User worker : workers) {
            long count = requestRepository.findByAssignedTo(worker).stream()
                    .filter(r -> r.getStatus() != null && "COMPLETED".equals(r.getStatus().getCode()))
                    .count();
            workerLoad.put(worker.getUsername() != null ? worker.getUsername() : "Unknown", count);
        }
        report.setWorkerLoad(workerLoad);

        return report;
    }

    /**
     * Экспорт отчёта в Excel
     */
    public byte[] exportToExcel(ReportDto report) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Стили
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Лист 1: Общая статистика
            Sheet summarySheet = workbook.createSheet("Общая статистика");
            int rowNum = 0;

            Row headerRow = summarySheet.createRow(rowNum++);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Показатель");
            headerCell.setCellStyle(headerStyle);
            headerCell = headerRow.createCell(1);
            headerCell.setCellValue("Значение");
            headerCell.setCellStyle(headerStyle);

            addSummaryRow(summarySheet, rowNum++, "Всего заявок", report.getTotalRequests());
            addSummaryRow(summarySheet, rowNum++, "Выполнено", report.getCompletedRequests());
            addSummaryRow(summarySheet, rowNum++, "Просрочено", report.getOverdueRequests());
            addSummaryRow(summarySheet, rowNum++, "В работе", report.getInProgressRequests());
            addSummaryRow(summarySheet, rowNum++, "Дата генерации", report.getGeneratedAt().toString());

            // Лист 2: Статистика по статусам
            Sheet statusSheet = workbook.createSheet("По статусам");
            rowNum = 0;
            headerRow = statusSheet.createRow(rowNum++);
            headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Статус");
            headerCell.setCellStyle(headerStyle);
            headerCell = headerRow.createCell(1);
            headerCell.setCellValue("Количество");
            headerCell.setCellStyle(headerStyle);

            for (Map.Entry<String, Long> entry : report.getRequestsByStatus().entrySet()) {
                Row row = statusSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            // Лист 3: Статистика по месяцам
            Sheet monthSheet = workbook.createSheet("По месяцам");
            rowNum = 0;
            headerRow = monthSheet.createRow(rowNum++);
            headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Месяц");
            headerCell.setCellStyle(headerStyle);
            headerCell = headerRow.createCell(1);
            headerCell.setCellValue("Количество заявок");
            headerCell.setCellStyle(headerStyle);

            for (Map.Entry<String, Long> entry : report.getRequestsByMonth().entrySet()) {
                Row row = monthSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            // Лист 4: Загрузка исполнителей
            Sheet workerSheet = workbook.createSheet("Загрузка исполнителей");
            rowNum = 0;
            headerRow = workerSheet.createRow(rowNum++);
            headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Исполнитель");
            headerCell.setCellStyle(headerStyle);
            headerCell = headerRow.createCell(1);
            headerCell.setCellValue("Выполнено заявок");
            headerCell.setCellStyle(headerStyle);

            for (Map.Entry<String, Long> entry : report.getWorkerLoad().entrySet()) {
                Row row = workerSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            // Авто-подбор ширины колонок
            for (int i = 0; i < 4; i++) {
                summarySheet.autoSizeColumn(i);
                statusSheet.autoSizeColumn(i);
                monthSheet.autoSizeColumn(i);
                workerSheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, long value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
}