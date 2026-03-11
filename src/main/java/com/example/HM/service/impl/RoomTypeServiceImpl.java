package com.example.HM.service.impl;

import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.entity.RoomType;
import com.example.HM.repository.RoomRepository;
import com.example.HM.repository.RoomTypeRepository;
import com.example.HM.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomTypeDTO> getAllRoomTypes(Pageable pageable) {
        return roomTypeRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomTypeDTO> searchRoomTypes(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return roomTypeRepository.findAll(pageable).map(this::convertToDTO);
        }
        return roomTypeRepository.findByTypeNameContainingIgnoreCase(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeDTO getRoomTypeById(String id) {
        return roomTypeRepository.findById(id)
                .map(this::convertToDTO)
                .orElseGet(() -> {
                    List<String> allIds = roomTypeRepository.findAll().stream()
                            .map(RoomType::getId)
                            .collect(Collectors.toList());
                    System.out.println("DEBUG: RoomType ID not found: " + id);
                    System.out.println("DEBUG: Available IDs: " + allIds);
                    throw new RuntimeException("Loại phòng không tồn tại! (ID: " + id + ")");
                });
    }

    @Override
    @Transactional
    public RoomTypeDTO createRoomType(RoomTypeDTO dto) {
        if (dto.getTypeName() == null || dto.getTypeName().isBlank()) {
            throw new RuntimeException("Tên loại phòng không được để trống!");
        }
        if (roomTypeRepository.existsByTypeName(dto.getTypeName())) {
            throw new RuntimeException("Tên loại phòng đã tồn tại!");
        }
        if (dto.getCapacity() == null || dto.getCapacity() <= 0) {
            throw new RuntimeException("Sức chứa phải lớn hơn 0!");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá phòng phải lớn hơn 0!");
        }

        RoomType roomType = new RoomType();
        roomType.setTypeName(dto.getTypeName());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        roomType.setPrice(dto.getPrice());
        roomType.setRoomImage(dto.getRoomImage());
        return convertToDTO(roomTypeRepository.save(roomType));
    }

    @Override
    @Transactional
    public RoomTypeDTO updateRoomType(String id, RoomTypeDTO dto) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loại phòng không tồn tại!"));

        if (dto.getTypeName() == null || dto.getTypeName().isBlank()) {
            throw new RuntimeException("Tên loại phòng không được để trống!");
        }
        if (roomTypeRepository.existsByTypeNameAndIdNot(dto.getTypeName(), id)) {
            throw new RuntimeException("Tên loại phòng đã tồn tại!");
        }
        if (dto.getCapacity() == null || dto.getCapacity() <= 0) {
            throw new RuntimeException("Sức chứa phải lớn hơn 0!");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá phòng phải lớn hơn 0!");
        }

        roomType.setTypeName(dto.getTypeName());
        roomType.setDescription(dto.getDescription());
        roomType.setCapacity(dto.getCapacity());
        roomType.setPrice(dto.getPrice());
        roomType.setRoomImage(dto.getRoomImage());
        return convertToDTO(roomTypeRepository.save(roomType));
    }

    @Override
    @Transactional
    public void deleteRoomType(String id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loại phòng không tồn tại!"));

        if (!roomRepository.findByRoomType(roomType, Pageable.unpaged()).isEmpty()) {
            throw new RuntimeException("Không thể xóa loại phòng vì đang có phòng sử dụng loại này!");
        }
        roomTypeRepository.deleteById(id);
    }

    private RoomTypeDTO convertToDTO(RoomType roomType) {
        long availableCount = roomRepository.countByRoomTypeAndStatus(roomType, "AVAILABLE");
        return RoomTypeDTO.builder()
                .id(roomType.getId())
                .typeName(roomType.getTypeName())
                .description(roomType.getDescription())
                .capacity(roomType.getCapacity())
                .price(roomType.getPrice())
                .roomImage(roomType.getRoomImage())
                .availableCount(availableCount)
                .build();
    }
}
