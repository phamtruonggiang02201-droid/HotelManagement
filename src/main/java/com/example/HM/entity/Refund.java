package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Refunds")
@Getter
@Setter
public class Refund extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PaymentID")
    private Payment payment;

    @Column(name = "RefundAmount", precision = 18, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "RequestedBy")
    private Integer requestedBy; // Giữ theo kiểu INT của SQL hoặc đổi UUID nếu cần

    @Column(name = "RequestedAt")
    private LocalDateTime requestedAt;

    @Column(name = "ProcessedAt")
    private LocalDateTime processedAt;
}
