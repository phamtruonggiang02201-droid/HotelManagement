package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Rooms")
@Getter
@Setter
public class Room extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomTypeID", nullable = false)
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @Column(name = "Status", length = 20)
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE

    @Column(name = "RoomName", length = 100)
    private String roomName;

    // Explicit getter for compiler issues
    public RoomType getRoomType() { return roomType; }
}
