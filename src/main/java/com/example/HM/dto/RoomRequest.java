package com.example.HM.dto;

import lombok.Data;

@Data
public class RoomRequest {
    private String roomName;
    private String roomTypeId;
    private String status;
    private String areaId;
}
