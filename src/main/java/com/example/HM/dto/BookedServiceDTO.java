package com.example.HM.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedServiceDTO {
    private String id;
    private String bookingId;
    private String guestName;
    private String roomNames; // Các phòng khách đang ở
    private String serviceName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String status; // ORDERED, DELIVERED, CANCELLED, COMPLETED
    private LocalDateTime createdAt;
}
