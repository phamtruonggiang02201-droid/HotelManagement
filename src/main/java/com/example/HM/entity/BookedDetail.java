package com.example.HM.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "BookedDetail")
@Getter
@Setter
public class BookedDetail extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookedServiceID", nullable = false)
    private BookedService bookedService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceID", nullable = false)
    private ExtraService service;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "UnitPrice", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "RatingComment", length = 500)
    private String ratingComment;

    @Column(name = "RatedAt")
    private java.time.LocalDateTime ratedAt;

    @Transient
    private String staffName;

    @Transient
    private String serviceStatus;
}
