package com.example.HM.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSummaryDTO {
    private String bookingId;
    private String guestName;
    private String roomTypeName;
    private String roomNames;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private long totalNights;
    
    private BigDecimal roomPricePerNight;
    private BigDecimal totalRoomPrice;
    
    private List<BookedServiceDTO> services;
    private BigDecimal totalServiceAmount;
    
    private BigDecimal totalAmount; // Tổng cộng cả phòng và dịch vụ
    private BigDecimal paidAmount;  // Số tiền khách đã thanh toán trước (VNPay)
    private BigDecimal balance;     // Số tiền còn lại phải trả
}
