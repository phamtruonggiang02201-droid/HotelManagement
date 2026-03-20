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

@Entity
@Table(name = "Booking")
@Getter
@Setter
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GuestID")
    private Guest guest;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookedRoom> bookedRooms = new ArrayList<>();

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
    private List<BookedService> bookedServices = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Occupant> occupants = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();
}
