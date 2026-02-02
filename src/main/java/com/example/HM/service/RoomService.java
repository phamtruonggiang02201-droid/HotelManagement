package com.example.HM.service;

import com.example.HM.dto.RoomDTO;
import com.example.HM.dto.RoomRequest;
import java.util.List;

public interface RoomService {
    List<RoomDTO> getAllRooms();
    List<RoomDTO> getRoomsByStatus(String status);
    RoomDTO getRoomById(String id);
    RoomDTO createRoom(RoomRequest request);
    RoomDTO updateRoom(String id, RoomRequest request);
    void deleteRoom(String id);
}
