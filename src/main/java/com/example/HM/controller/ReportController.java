package com.example.HM.controller;

import com.example.HM.dto.StatisticsDTO;
import com.example.HM.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/admin/statistics")
    public String showStatisticsPage(Model model) {
        // Initial load with current month data
        StatisticsDTO stats = reportService.getStatistics("month");
        model.addAttribute("stats", stats);
        model.addAttribute("period", "month");
        return "admin/statistics";
    }

    @GetMapping("/api/admin/reports/data")
    @ResponseBody
    public ResponseEntity<StatisticsDTO> getReportData(@RequestParam(defaultValue = "month") String period) {
        StatisticsDTO stats = reportService.getStatistics(period);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/admin/reports/revenue/export")
    @ResponseBody
    public ResponseEntity<Resource> exportRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        String filename = "luxe-stay-revenue-report.xlsx";
        ByteArrayInputStream in = reportService.exportRevenueReport(start, end);
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/api/admin/reports/occupancy/export")
    @ResponseBody
    public ResponseEntity<Resource> exportOccupancyReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        String filename = "luxe-stay-occupancy-report.xlsx";
        ByteArrayInputStream in = reportService.exportOccupancyReport(start, end);
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
