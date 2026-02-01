package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "BookingHistory")
@Getter
@Setter
public class BookingHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID")
    private Booking booking;

    @Column(name = "OldStatus", length = 20)
    private String oldStatus;

    @Column(name = "NewStatus", length = 20)
    private String newStatus;

    @Column(name = "ChangedAt")
    private LocalDateTime changedAt;

    @Column(name = "ChangedBy")
    private Integer changedBy;
}
