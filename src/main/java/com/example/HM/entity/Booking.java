package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Booking")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID")
    @JsonIgnore
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GuestID")
    @JsonIgnoreProperties({"bookings", "hibernateLazyInitializer", "handler"})
    private Guest guest;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookedRoom> bookedRooms = new LinkedHashSet<>();

    @Column(name = "CheckIn")
    private LocalDate checkIn;

    @Column(name = "CheckOut")
    private LocalDate checkOut;

    @ManyToMany
    @JoinTable(
        name = "BookingRoom",
        joinColumns = @JoinColumn(name = "BookingID"),
        inverseJoinColumns = @JoinColumn(name = "RoomID")
    )
    private Set<Room> rooms;

    @Column(name = "TotalAmount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "Status", length = 30)
    private String status; // PENDING_PAYMENT, PAID, CANCELLED, COMPLETED

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "PaidAmount", precision = 18, scale = 2)
    private BigDecimal paidAmount;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private Set<BookedService> bookedServices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Occupant> occupants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"booking", "hibernateLazyInitializer", "handler"})
    private Set<Payment> payments = new LinkedHashSet<>();

    // Explicit getters for compiler issues
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
}
