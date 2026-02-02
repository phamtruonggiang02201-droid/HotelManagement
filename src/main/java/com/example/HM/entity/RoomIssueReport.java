package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "RoomIssueReports")
@Getter
@Setter
public class RoomIssueReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomID")
    private Room room;

    @Column(name = "CreatedBy")
    private Integer createdBy;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "ResolvedAt")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID")
    private Booking booking;
}
