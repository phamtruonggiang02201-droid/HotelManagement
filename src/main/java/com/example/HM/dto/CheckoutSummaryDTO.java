package com.example.HM.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private LocalDate checkOut; // Dự kiếnban đầu
    private LocalDate actualCheckOut; // Thực tế
    private long totalNights; // Dự kiến
    private long actualNights; // Thực tế
    
    private BigDecimal roomPricePerNight;
    private BigDecimal totalRoomPrice;
    private BigDecimal roomPriceAdjustment; // Số tiền chênh lệch (thường là âm nếu trả phòng sớm)
    
    private List<BookedServiceDTO> services;
    private BigDecimal totalServiceAmount;
    
    private List<OccupantDTO> occupants;
    
    private BigDecimal totalAmount; // Tổng cộng thực tế sau điều chỉnh
    private BigDecimal originalTotalAmount; // Tổng cộng ban đầu
    private BigDecimal paidAmount;  // Số tiền khách đã thanh toán trước (VNPay)
    private BigDecimal balance;     // Số tiền còn lại phải trả
    
    @Builder.Default
    private List<BookedRoomSummaryDTO> bookedRooms = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookedRoomSummaryDTO {
        private String roomTypeName;
        private int quantity;
        private BigDecimal priceAtBooking;
        private BigDecimal subTotal; // (price * quantity * nights)
    }
}
