package com.example.HM.service.impl;

import com.example.HM.dto.RoomDTO;
import com.example.HM.dto.RoomRequest;
import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.entity.Room;
import com.example.HM.entity.RoomType;
import com.example.HM.repository.RoomRepository;
import com.example.HM.repository.RoomTypeRepository;
import com.example.HM.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomDTO> getAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomDTO> searchRooms(String keyword, String typeId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable) {
        return roomRepository.searchRoomsAdvanced(keyword, typeId, minPrice, maxPrice, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomDTO> getRoomsByStatus(String status, Pageable pageable) {
        return roomRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDTO getRoomById(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));
        return convertToDTO(room);
    }

    @Override
    @Transactional
    public RoomDTO createRoom(RoomRequest request) {
        if (request.getRoomName() == null || request.getRoomName().isBlank()) {
            throw new RuntimeException("Tên phòng không được để trống!");
        }
        if (roomRepository.existsByRoomName(request.getRoomName())) {
            throw new RuntimeException("Tên phòng đã tồn tại!");
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Loại phòng không hợp lệ!"));

        Room room = new Room();
        room.setRoomName(request.getRoomName());
        room.setRoomType(roomType);
        room.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");

        return convertToDTO(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(String id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));

        if (request.getRoomName() == null || request.getRoomName().isBlank()) {
            throw new RuntimeException("Tên phòng không được để trống!");
        }
        if (roomRepository.existsByRoomNameAndIdNot(request.getRoomName(), id)) {
            throw new RuntimeException("Tên phòng đã tồn tại!");
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Loại phòng không hợp lệ!"));

        room.setRoomName(request.getRoomName());
        room.setRoomType(roomType);
        room.setStatus(request.getStatus());

        return convertToDTO(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void deleteRoom(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));

        if (roomRepository.hasBookings(id)) {
            throw new RuntimeException("Không thể xóa phòng vì đã có lịch sử đặt phòng!");
        }
        roomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomDTO> getRoomsByRoomTypeAndStatus(String roomTypeId, String status, Pageable pageable) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Loại phòng không tồn tại!"));
        return roomRepository.findByRoomTypeAndStatus(roomType, status, pageable)
                .map(this::convertToDTO);
    }

    private RoomDTO convertToDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .status(room.getStatus())
                .roomType(RoomTypeDTO.builder()
                        .id(room.getRoomType().getId())
                        .typeName(room.getRoomType().getTypeName())
                        .capacity(room.getRoomType().getCapacity())
                        .price(room.getRoomType().getPrice())
                        .roomImage(room.getRoomType().getRoomImage())
                        .build())
                .build();
    }
}
