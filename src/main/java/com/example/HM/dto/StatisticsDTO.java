package com.example.HM.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private Double totalRevenue;
    private Long totalBookings;
    private Long totalCheckIns;
    private Double checkInRate;
    
    // Dữ liệu cho biểu đồ đường/cột
    private List<String> labels;
    private List<Double> revenueData;
    private List<Long> bookingData;
    
    // Dữ liệu cho biểu đồ tròn (Dịch vụ)
    private List<String> serviceLabels;
    private List<Long> serviceUsageData;
    
    // Thống kê chi tiết theo loại phòng
    private Map<String, Long> roomTypeStats;
}
