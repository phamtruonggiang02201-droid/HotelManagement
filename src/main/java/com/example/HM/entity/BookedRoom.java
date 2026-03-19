package com.example.HM.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "BookedRoom")
@Getter
@Setter
public class BookedRoom extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomTypeID", nullable = false)
    private RoomType roomType;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "PriceAtBooking", precision = 18, scale = 2, nullable = false)
    private BigDecimal priceAtBooking;
}
