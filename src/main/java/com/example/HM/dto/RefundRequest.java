package com.example.HM.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundRequest {
    private String bookingId;
    private String reason;
    private BigDecimal amount;
}
