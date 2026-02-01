package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BookingService")
@Getter
@Setter
public class BookingService extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceID", nullable = false)
    private Service service;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "UnitPrice", precision = 18, scale = 2)
    private BigDecimal unitPrice;
}
