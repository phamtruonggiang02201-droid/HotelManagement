package com.example.HM.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackRequest {
    private String bookingId;
    private Integer rating;
    private String comment;
    private String roomTypeId;
    private String serviceId;
}
