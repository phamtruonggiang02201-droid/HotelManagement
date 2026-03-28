package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Invoices")
@Getter
@Setter
@RequiredArgsConstructor
public class Invoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID")
    private Booking booking;

    @Column(name = "Amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "Date")
    private LocalDateTime date;
}
