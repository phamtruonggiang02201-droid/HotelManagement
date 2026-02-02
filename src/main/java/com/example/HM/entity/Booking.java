package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.Set;

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
}
