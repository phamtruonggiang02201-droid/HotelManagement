package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Getter
@Setter
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID")
    private Booking booking;

    @Column(name = "Amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "PaymentStatus", length = 20)
    private String paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PaymentMethodID")
    private PaymentMethod paymentMethod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InvoiceID")
    private Invoice invoice;
}
