package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RevenueReports")
@Getter
@Setter
public class RevenueReport extends BaseEntity {

    @Column(name = "PeriodType", length = 20)
    private String periodType;

    @Column(name = "PeriodValue", length = 50)
    private String periodValue;

    @Column(name = "TotalRevenue", precision = 18, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "CreatedBy")
    private Integer createdBy;
}
