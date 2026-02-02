package com.example.HM.service.impl;

import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.entity.RoomType;
import com.example.HM.repository.RoomTypeRepository;
import com.example.HM.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeDTO> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeDTO getRoomTypeById(String id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loại phòng không tồn tại!"));
        return convertToDTO(roomType);
    }

    @Override
    @Transactional
    public RoomTypeDTO createRoomType(RoomTypeDTO dto) {
        RoomType roomType = new RoomType();
        roomType.setTypeName(dto.getTypeName());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        return convertToDTO(roomTypeRepository.save(roomType));
    }

    @Override
    @Transactional
    public RoomTypeDTO updateRoomType(String id, RoomTypeDTO dto) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loại phòng không tồn tại!"));
        roomType.setTypeName(dto.getTypeName());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        return convertToDTO(roomTypeRepository.save(roomType));
    }

    @Override
    @Transactional
    public void deleteRoomType(String id) {
        roomTypeRepository.deleteById(id);
    }

    private RoomTypeDTO convertToDTO(RoomType roomType) {
        return RoomTypeDTO.builder()
                .id(roomType.getId())
                .typeName(roomType.getTypeName())
                .description(roomType.getDescription())
                .capacity(roomType.getCapacity())
                .build();
    }
}
