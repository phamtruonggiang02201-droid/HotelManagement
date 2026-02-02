package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "Services")
@Getter
@Setter
public class Service extends BaseEntity {

    @Column(name = "ServiceName", length = 100)
    private String serviceName;

    @Column(name = "Price", precision = 18, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID")
    private ServiceCategory category;
}
