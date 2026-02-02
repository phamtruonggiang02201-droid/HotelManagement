package com.example.HM.service;

import com.example.HM.dto.RoomTypeDTO;
import java.util.List;

public interface RoomTypeService {
    List<RoomTypeDTO> getAllRoomTypes();
    RoomTypeDTO getRoomTypeById(String id);
    RoomTypeDTO createRoomType(RoomTypeDTO roomTypeDTO);
    RoomTypeDTO updateRoomType(String id, RoomTypeDTO roomTypeDTO);
    void deleteRoomType(String id);
}
