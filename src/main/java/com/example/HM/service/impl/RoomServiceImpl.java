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
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDTO> getRoomsByStatus(String status) {
        return roomRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Loại phòng không hợp lệ!"));
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new IllegalArgumentException("Tên phòng đã tồn tại");
        }
        Room room = new Room();
        room.setRoomName(request.getRoomName());
        room.setRoomType(roomType);
        room.setPrice(request.getPrice());
        room.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");
        room.setRoomImage(request.getRoomImage());
        
        return convertToDTO(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(String id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));
        
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Loại phòng không hợp lệ!"));
                
        room.setRoomName(request.getRoomName());
        room.setRoomType(roomType);
        room.setPrice(request.getPrice());
        room.setStatus(request.getStatus());
        room.setRoomImage(request.getRoomImage());
        
        return convertToDTO(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void deleteRoom(String id) {
        roomRepository.deleteById(id);
    }

    private RoomDTO convertToDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .status(room.getStatus())
                .price(room.getPrice())
                .roomImage(room.getRoomImage())
                .roomType(RoomTypeDTO.builder()
                        .id(room.getRoomType().getId())
                        .typeName(room.getRoomType().getTypeName())
                        .capacity(room.getRoomType().getCapacity())
                        .build())
                .build();
    }
}
