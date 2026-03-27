package com.example.HM.entity;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class BookingService extends BaseEntity {

    private Booking booking;

    private Service service;

    private Integer quantity;

    private String status;

    private BigDecimal unitPrice;
}
