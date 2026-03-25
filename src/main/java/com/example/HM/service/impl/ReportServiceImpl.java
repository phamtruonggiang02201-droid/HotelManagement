package com.example.HM.service.impl;

import com.example.HM.dto.StatisticsDTO;
import com.example.HM.repository.BookingRepository;
import com.example.HM.repository.PaymentRepository;
import com.example.HM.repository.BookedServiceRepository;
import com.example.HM.repository.RoomRepository;
import com.example.HM.service.ReportService;
import com.example.HM.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookedServiceRepository bookedServiceRepository;
    private final RoomRepository roomRepository;

    @Override
    public StatisticsDTO getStatistics(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        LocalDateTime endDate = now.with(LocalTime.MAX);

        switch (period.toLowerCase()) {
            case "month":
                startDate = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
                break;
            case "quarter":
                int month = now.getMonthValue();
                int startMonth = ((month - 1) / 3) * 3 + 1;
                startDate = LocalDateTime.of(now.getYear(), startMonth, 1, 0, 0);
                break;
            case "year":
                startDate = now.with(TemporalAdjusters.firstDayOfYear()).with(LocalTime.MIN);
                break;
            default:
                startDate = now.minusDays(30).with(LocalTime.MIN); // Default last 30 days
        }

        StatisticsDTO stats = new StatisticsDTO();
        
        // Basic Counts
        Object revenueObj = paymentRepository.sumRevenueBetween(startDate, endDate);
        stats.setTotalRevenue(revenueObj != null ? ((Number) revenueObj).doubleValue() : 0.0);
        
        Long totalBookings = bookingRepository.countBookingsBetween(startDate, endDate);
        stats.setTotalBookings(totalBookings != null ? totalBookings : 0L);
        
        Long totalCheckIns = bookingRepository.countCheckInsBetween(startDate.toLocalDate(), endDate.toLocalDate());
        stats.setTotalCheckIns(totalCheckIns != null ? totalCheckIns : 0L);
        
        if (totalBookings != null && totalBookings > 0) {
            stats.setCheckInRate((double) totalCheckIns / totalBookings * 100);
        } else {
            stats.setCheckInRate(0.0);
        }

        // Charts Data - Daily Breakdown
        List<Object[]> dailyRevenue = paymentRepository.getDailyRevenueBetween(startDate, endDate);
        List<Object[]> dailyBookings = bookingRepository.getDailyBookingCountBetween(startDate, endDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        // Merge labels from both to ensure consistency (Simplified for now)
        stats.setLabels(dailyRevenue.stream()
                .map(obj -> {
                    Object dateObj = obj[0];
                    if (dateObj instanceof java.time.LocalDate) {
                        return ((java.time.LocalDate) dateObj).format(formatter);
                    } else if (dateObj instanceof java.sql.Date) {
                        return ((java.sql.Date) dateObj).toLocalDate().format(formatter);
                    } else if (dateObj instanceof LocalDateTime) {
                        return ((LocalDateTime) dateObj).format(formatter);
                    }
                    return dateObj.toString();
                })
                .collect(Collectors.toList()));
        
        stats.setRevenueData(dailyRevenue.stream()
                .map(obj -> obj[1] != null ? ((Number) obj[1]).doubleValue() : 0.0)
                .collect(Collectors.toList()));
                
        stats.setBookingData(dailyBookings.stream()
                .map(obj -> (Long) obj[1])
                .collect(Collectors.toList()));

        // Top Services
        List<Object[]> topServices = bookedServiceRepository.getTopServices(PageRequest.of(0, 5));
        stats.setServiceLabels(topServices.stream()
                .map(obj -> (String) obj[0])
                .collect(Collectors.toList()));
        stats.setServiceUsageData(topServices.stream()
                .map(obj -> (Long) obj[1])
                .collect(Collectors.toList()));

        // Room Type Stats (Bookings by Room Type)
        List<Object[]> roomTypeData = bookingRepository.countBookingsByRoomType(startDate, endDate);
        java.util.Map<String, Long> roomTypeStats = new java.util.LinkedHashMap<>();
        for (Object[] row : roomTypeData) {
            roomTypeStats.put((String) row[0], (Long) row[1]);
        }
        stats.setRoomTypeStats(roomTypeStats);

        return stats;
    }

    @Override
    public ByteArrayInputStream exportRevenueReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        List<Object[]> dailyRevenue = paymentRepository.getDailyRevenueBetween(start, end);
        String[] headers = { "Ngày", "Doanh thu (VNĐ)" };
        
        return ExcelHelper.dataToExcel(dailyRevenue, "RevenueReport", headers, (row, obj) -> {
            row.createCell(0).setCellValue(obj[0].toString());
            row.createCell(1).setCellValue(obj[1] != null ? ((Number) obj[1]).doubleValue() : 0.0);
        });
    }

    @Override
    public ByteArrayInputStream exportOccupancyReport(LocalDate startDate, LocalDate endDate) {
        // Một báo cáo đơn giản về tỉ lệ lấp đầy theo ngày
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        long totalRooms = roomRepository.count();
        String[] headers = { "Ngày", "Số phòng đang ở", "Tổng số phòng", "Tỉ lệ (%)" };

        return ExcelHelper.dataToExcel(dates, "OccupancyReport", headers, (row, date) -> {
            Long occupiedCount = bookingRepository.countOccupiedRoomsAtDate(date);
            double rate = totalRooms > 0 ? (double) occupiedCount / totalRooms * 100 : 0.0;
            
            row.createCell(0).setCellValue(date.toString());
            row.createCell(1).setCellValue(occupiedCount != null ? occupiedCount : 0);
            row.createCell(2).setCellValue(totalRooms);
            row.createCell(3).setCellValue(Math.round(rate * 100.0) / 100.0);
        });
    }
}
