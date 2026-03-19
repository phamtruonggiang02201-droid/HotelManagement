package com.example.HM.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private String id;
    private String bookingId;
    private String guestName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String paymentStatus;
    private String paymentMethod;
}
