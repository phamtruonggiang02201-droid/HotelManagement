package com.example.HM.service.impl;

import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.entity.RoomType;
import com.example.HM.entity.RoomTypeImage;
import com.example.HM.repository.RoomTypeRepository;
import com.example.HM.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
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
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Override
    public ByteArrayInputStream exportRoomTypesToExcel() {
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        String[] headers = { "ID", "Tên loại phòng", "Giá cơ bản", "Số lượng phòng", "Mô tả" };

        return ExcelHelper.dataToExcel(roomTypes, "RoomTypes", headers, (row, rt) -> {
            row.createCell(0).setCellValue(rt.getId());
            row.createCell(1).setCellValue(rt.getTypeName());
            row.createCell(2).setCellValue(rt.getPrice() != null ? rt.getPrice().doubleValue() : 0.0);
            row.createCell(3).setCellValue(0); // RoomType entity doesn't have totalCount field
            row.createCell(4).setCellValue(rt.getDescription());
        });
    }
    @Override
    @Transactional
    public void importRoomTypesFromExcel(MultipartFile file) {
        try {
            List<RoomType> roomTypes = ExcelHelper.excelToData(file.getInputStream(), "RoomTypes", row -> {
                String typeName = ExcelHelper.getCellValueAsString(row.getCell(1));
                if (typeName.isEmpty()) return null;

                RoomType rt = new RoomType();
                rt.setTypeName(typeName);
                rt.setPrice(new java.math.BigDecimal(ExcelHelper.getCellValueAsString(row.getCell(2))));
                rt.setDescription(ExcelHelper.getCellValueAsString(row.getCell(4)));
                return rt;
            });

            // Skip existing names
            roomTypes.removeIf(rt -> roomTypeRepository.findAll().stream().anyMatch(existing -> existing.getTypeName().equalsIgnoreCase(rt.getTypeName())));
            roomTypeRepository.saveAll(roomTypes);
        } catch (IOException e) {
            throw new RuntimeException("Could not store the data: " + e.getMessage());
        }
    }

}
