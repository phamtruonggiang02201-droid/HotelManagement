package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Feedbacks")
@Getter
@Setter
public class Feedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingID")
    private Booking booking;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "Comment", length = 1000)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomTypeID")
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceID")
    private ExtraService extraService;

    @Column(name = "AdminReply", length = 1000)
    private String adminReply;

    @Column(name = "RepliedAt")
    private java.time.LocalDateTime repliedAt;
}
