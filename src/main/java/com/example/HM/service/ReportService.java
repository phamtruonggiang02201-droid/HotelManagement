package com.example.HM.service;

import com.example.HM.dto.StatisticsDTO;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;

public interface ReportService {
    StatisticsDTO getStatistics(String period);
    
    // Excel Reports
    ByteArrayInputStream exportRevenueReport(LocalDate startDate, LocalDate endDate);
    ByteArrayInputStream exportOccupancyReport(LocalDate startDate, LocalDate endDate);
}
