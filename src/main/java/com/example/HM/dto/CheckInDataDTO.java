package com.example.HM.dto;

import com.example.HM.entity.Booking;
import com.example.HM.entity.Room;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInDataDTO {
    private Booking booking;
    private List<RoomGroupDTO> roomGroups;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomGroupDTO {
        private String roomTypeName;
        private Integer requiredQuantity;
        private List<RoomStatusDTO> rooms;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomStatusDTO {
        private String id;
        private String roomName;
        private boolean occupied;
    }
}
