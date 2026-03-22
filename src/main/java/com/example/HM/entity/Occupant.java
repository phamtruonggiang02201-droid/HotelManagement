package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Occupant")
@Getter
@Setter
@NoArgsConstructor
public class Occupant extends BaseEntity {

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomID", nullable = false)
    private Room room;

    @Column(name = "FullName", length = 100)
    private String fullName;

    @Column(name = "IdNumber", length = 50)
    private String idNumber;

    @Column(name = "Phone", length = 20)
    private String phone;
}
