package com.example.HM.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BookingService")
@Getter
@Setter
public class BookedService extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomID")
    private Room room;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "TotalAmount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "bookedService", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<BookedDetail> details = new java.util.ArrayList<>();

    // Explicit getters for compiler issues
    public Booking getBooking() { return booking; }
    public java.util.List<BookedDetail> getDetails() { return details; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Room getRoom() { return room; }
}
