package com.example.HM.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckInRequest {
    private String bookingId;
    private List<String> roomIds;
    private String guestIdCard;
    private String guestFullName;
    private String note;
}
