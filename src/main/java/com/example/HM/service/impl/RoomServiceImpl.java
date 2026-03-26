package com.example.HM.service.impl;

import com.example.HM.dto.RoomDTO;
import com.example.HM.dto.RoomRequest;
import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.entity.Area;
import com.example.HM.entity.Room;
import com.example.HM.entity.RoomType;
import com.example.HM.repository.AreaRepository;
import com.example.HM.repository.RoomRepository;
import com.example.HM.repository.RoomTypeRepository;
import com.example.HM.service.RoomService;
import com.example.HM.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.HM.util.ExcelHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AreaRepository areaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomDTO> getAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomDTO> searchRooms(String keyword, String typeId, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, java.time.LocalDate date, String areaId, Pageable pageable) {
        Page<Room> rooms = roomRepository.searchRoomsAdvanced(keyword, typeId, minPrice, maxPrice, areaId, pageable);

        if (date != null) {
            List<String> occupiedRoomIds = roomRepository.findOccupiedRoomIdsByDate(date);
            List<Object[]> bookedQuantities = roomRepository.findBookedQuantitiesByDate(date);
            
            // Map RoomTypeID -> Total Booked Quantity
            java.util.Map<String, Long> totalBookedByType = new java.util.HashMap<>();
            for (Object[] row : bookedQuantities) {
                totalBookedByType.put((String) row[0], ((Number) row[1]).longValue());
            }

            // Count how many rooms are already assigned per type for this date
            java.util.Map<String, Long> assignedCountByType = new java.util.HashMap<>();
            for (String roomId : occupiedRoomIds) {
                roomRepository.findById(roomId).ifPresent(r -> {
                    String rtId = r.getRoomType().getId();
                    assignedCountByType.put(rtId, assignedCountByType.getOrDefault(rtId, 0L) + 1);
                });
            }

            // Identify all rooms and their types to handle "floating" reservations deterministically
            List<Room> allRooms = roomRepository.findAll();
            java.util.Map<String, List<Room>> availableRoomsByType = new java.util.HashMap<>();
            
            for (Room r : allRooms) {
                if (!"MAINTENANCE".equals(r.getStatus()) && !occupiedRoomIds.contains(r.getId())) {
                    String rtId = r.getRoomType().getId();
                    availableRoomsByType.computeIfAbsent(rtId, k -> new java.util.ArrayList<>()).add(r);
                }
            }
            
            // Sort available rooms by name for deterministic "floating" assignment
            availableRoomsByType.values().forEach(list -> list.sort(java.util.Comparator.comparing(Room::getRoomName)));

            // Map each RoomID to its "floating" status
            java.util.Set<String> floatingReservedIds = new java.util.HashSet<>();
            totalBookedByType.forEach((rtId, total) -> {
                long assigned = assignedCountByType.getOrDefault(rtId, 0L);
                long needed = total - assigned;
                if (needed > 0) {
                    List<Room> available = availableRoomsByType.getOrDefault(rtId, java.util.Collections.emptyList());
                    for (int i = 0; i < Math.min(needed, available.size()); i++) {
                        floatingReservedIds.add(available.get(i).getId());
                    }
                }
            });

            return rooms.map(room -> {
                RoomDTO dto = convertToDTO(room);
                if (occupiedRoomIds.contains(room.getId()) || floatingReservedIds.contains(room.getId())) {
                    dto.setStatus("OCCUPIED");
                } else if (!"MAINTENANCE".equals(room.getStatus())) {
                    dto.setStatus("AVAILABLE");
                }
                return dto;
            });
        }

        return rooms.map(this::convertToDTO);
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
        room.setRoomName(request.getRoomName().trim());
        room.setRoomType(roomType);
        room.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");
        if (request.getAreaId() != null && !request.getAreaId().isBlank()) {
            areaRepository.findById(request.getAreaId()).ifPresent(room::setArea);
        }

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

        room.setRoomName(request.getRoomName().trim());
        room.setRoomType(roomType);
        room.setStatus(request.getStatus() != null ? request.getStatus() : room.getStatus());
        if (request.getAreaId() != null && !request.getAreaId().isBlank()) {
            areaRepository.findById(request.getAreaId()).ifPresent(room::setArea);
        } else {
            room.setArea(null);
        }

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

    @Override
    @Transactional(readOnly = true)
    public List<RoomDTO> getAvailableRoomsByType(String typeId, java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        return roomRepository.findAvailableRoomsByType(typeId, checkIn, checkOut).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDTO> getRoomStatusByDate(java.time.LocalDate date) {
        List<Room> allRooms = roomRepository.findAll();
        List<String> occupiedRoomIds = roomRepository.findOccupiedRoomIdsByDate(date);

        return allRooms.stream().map(room -> {
            RoomDTO dto = convertToDTO(room);
            // Nếu phòng đang trong danh sách bận của ngày đó
            if (occupiedRoomIds.contains(room.getId())) {
                dto.setStatus("OCCUPIED");
            } else if (!"MAINTENANCE".equals(room.getStatus())) {
                // Nếu phòng không bận và không bảo trì thì coi là trống vào ngày đó
                dto.setStatus("AVAILABLE");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.example.HM.dto.RoomStatsDTO getRoomStatsByDate(java.time.LocalDate date) {
        List<Room> allRooms = roomRepository.findAll();
        List<String> occupiedRoomIds = roomRepository.findOccupiedRoomIdsByDate(date);
        
        long total = allRooms.size();
        long maintenance = allRooms.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();
        
        List<Object[]> bookedQuantities = roomRepository.findBookedQuantitiesByDate(date);
        long totalBooked = 0;
        for (Object[] row : bookedQuantities) {
            totalBooked += ((Number) row[1]).longValue();
        }
        
        long occupied = Math.max(occupiedRoomIds.size(), totalBooked);
        long available = Math.max(0, total - occupied - maintenance);

        return com.example.HM.dto.RoomStatsDTO.builder()
                .totalRooms(total)
                .availableRooms(available)
                .occupiedRooms(occupied)
                .maintenanceRooms(maintenance)
                .build();
    }

    @Override
    public ByteArrayInputStream exportRoomsToExcel() {
        List<Room> rooms = roomRepository.findAll();
        String[] headers = { "ID", "Tên phòng", "Loại phòng", "Trạng thái" };
        
        return ExcelHelper.dataToExcel(rooms, "Rooms", headers, (row, r) -> {
            row.createCell(0).setCellValue(r.getId());
            row.createCell(1).setCellValue(r.getRoomName());
            row.createCell(2).setCellValue(r.getRoomType() != null ? r.getRoomType().getTypeName() : "N/A");
            row.createCell(3).setCellValue(r.getStatus());
        });
    }

    @Override
    @Transactional
    public String importRoomsFromExcel(MultipartFile file) {
        try {
            List<Room> allRooms = ExcelHelper.excelToData(file.getInputStream(), "Rooms", row -> {
                String roomName = ExcelHelper.getCellValueAsString(row.getCell(1));
                if (roomName == null || roomName.trim().isEmpty()) return null;
                
                Room r = new Room();
                r.setRoomName(roomName.trim());
                
                String typeName = ExcelHelper.getCellValueAsString(row.getCell(2));
                RoomType rt = roomTypeRepository.findAll().stream()
                        .filter(t -> t.getTypeName().equalsIgnoreCase(typeName))
                        .findFirst()
                        .orElse(null);
                r.setRoomType(rt);
                
                r.setStatus(ExcelHelper.getCellValueAsString(row.getCell(3)));
                return r;
            });

            int duplicateCount = 0;
            List<Room> toSave = new ArrayList<>();
            for (Room r : allRooms) {
                if (roomRepository.existsByRoomName(r.getRoomName())) {
                    duplicateCount++;
                } else {
                    toSave.add(r);
                }
            }
            
            roomRepository.saveAll(toSave);
            return String.format("Nhập dữ liệu thành công! Đã thêm %d phòng mới. Bỏ qua %d phòng do trùng tên.", 
                    toSave.size(), duplicateCount);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }
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
                .areaId(room.getArea() != null ? room.getArea().getId() : null)
                .areaName(room.getArea() != null ? room.getArea().getAreaName() : null)
                .build();
    }
    @Override
    @Transactional
    public void assignDefaultAreaToOldRooms() {
        List<Room> roomsWithoutArea = roomRepository.findAll().stream()
                .filter(r -> r.getArea() == null)
                .collect(Collectors.toList());
        
        if (roomsWithoutArea.isEmpty()) return;

        Area defaultArea = areaRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        if (defaultArea != null) {
            roomsWithoutArea.forEach(r -> r.setArea(defaultArea));
            roomRepository.saveAll(roomsWithoutArea);
        }
    }
}
