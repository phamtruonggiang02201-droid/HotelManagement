package com.example.HM.service;

import com.example.HM.dto.RoomTypeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomTypeService {
    Page<RoomTypeDTO> getAllRoomTypes(Pageable pageable);
    Page<RoomTypeDTO> searchRoomTypes(String keyword, Pageable pageable);
    RoomTypeDTO getRoomTypeById(String id);
    RoomTypeDTO createRoomType(RoomTypeDTO roomTypeDTO);
    RoomTypeDTO updateRoomType(String id, RoomTypeDTO roomTypeDTO);
    void deleteRoomType(String id);
}
