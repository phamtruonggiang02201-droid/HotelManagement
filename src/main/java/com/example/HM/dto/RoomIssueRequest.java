package com.example.HM.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomIssueRequest {
    private String bookingId;
    private String roomId;
    private String description;
}
