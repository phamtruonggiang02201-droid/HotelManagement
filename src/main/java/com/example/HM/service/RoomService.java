package com.example.HM.service;

import com.example.HM.dto.RoomDTO;
import com.example.HM.dto.RoomRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface RoomService {
    Page<RoomDTO> getAllRooms(Pageable pageable);
    Page<RoomDTO> searchRooms(String keyword, String typeId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, java.time.LocalDate date, String areaId, Pageable pageable);
    Page<RoomDTO> getRoomsByStatus(String status, Pageable pageable);
    RoomDTO getRoomById(String id);
    RoomDTO createRoom(RoomRequest request);
    RoomDTO updateRoom(String id, RoomRequest request);
    void deleteRoom(String id);
    Page<RoomDTO> getRoomsByRoomTypeAndStatus(String roomTypeId, String status, Pageable pageable);
    List<RoomDTO> getAvailableRoomsByType(String typeId, java.time.LocalDate checkIn, java.time.LocalDate checkOut);
    List<RoomDTO> getRoomStatusByDate(java.time.LocalDate date); // Lấy trạng thái phòng theo ngày
    com.example.HM.dto.RoomStatsDTO getRoomStatsByDate(java.time.LocalDate date);

    // Excel Operations
    ByteArrayInputStream exportRoomsToExcel();
    String importRoomsFromExcel(MultipartFile file);

    void assignDefaultAreaToOldRooms();
}
