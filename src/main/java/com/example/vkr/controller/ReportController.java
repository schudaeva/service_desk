package com.example.vkr.controller;

import com.example.vkr.dto.ReportDto;
import com.example.vkr.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String showReports(Model model) {
        ReportDto report = reportService.collectStatistics();
        model.addAttribute("report", report);
        model.addAttribute("generatedDate", report.getGeneratedAt()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

        // Отладочный вывод
        System.out.println("Statuses: " + report.getRequestsByStatus());
        System.out.println("Months: " + report.getRequestsByMonth());

        return "reports/index";
    }
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel() {
        ReportDto report = reportService.collectStatistics();
        byte[] excelData = reportService.exportToExcel(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_" +
                        java.time.LocalDate.now() + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }
}