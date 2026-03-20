package com.example.HM.service.impl;

import com.example.HM.dto.RoomDTO;
import com.example.HM.dto.RoomRequest;
import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.entity.Room;
import com.example.HM.entity.RoomType;
import com.example.HM.repository.RoomRepository;
import com.example.HM.repository.RoomTypeRepository;
import com.example.HM.service.RoomService;
import com.example.HM.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public RoomDTO getRoomById(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));
        return convertToDTO(room);
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
    public void importRoomsFromExcel(MultipartFile file) {
        try {
            List<Room> rooms = ExcelHelper.excelToData(file.getInputStream(), "Rooms", row -> {
                String roomName = ExcelHelper.getCellValueAsString(row.getCell(1));
                if (roomName.isEmpty()) return null;

                Room r = new Room();
                r.setRoomName(roomName);

                String typeName = ExcelHelper.getCellValueAsString(row.getCell(2));
                RoomType rt = roomTypeRepository.findAll().stream()
                        .filter(t -> t.getTypeName().equalsIgnoreCase(typeName))
                        .findFirst()
                        .orElse(null);
                r.setRoomType(rt);

                r.setStatus(ExcelHelper.getCellValueAsString(row.getCell(3)));
                return r;
            });

            // Skip existing names
            rooms.removeIf(r -> roomRepository.existsByRoomName(r.getRoomName()));
            roomRepository.saveAll(rooms);
        } catch (IOException e) {
            throw new RuntimeException("Could not store the data: " + e.getMessage());
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
}
