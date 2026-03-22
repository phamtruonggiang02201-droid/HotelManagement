package com.example.HM.service.impl;

import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.entity.RoomType;
import com.example.HM.entity.RoomTypeImage;
import com.example.HM.repository.RoomRepository;
import com.example.HM.repository.RoomTypeRepository;
import com.example.HM.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.HM.util.ExcelHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final com.example.HM.repository.FeedbackRepository feedbackRepository;
    private final com.example.HM.repository.BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RoomTypeDTO> getAllRoomTypes(Pageable pageable) {
        return roomTypeRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomTypeDTO> searchRoomTypes(String keyword, String typeId, java.time.LocalDate checkIn, java.time.LocalDate checkOut, Pageable pageable) {
        Page<RoomType> roomTypes = roomTypeRepository.searchRoomTypes(keyword, typeId, pageable);
        return roomTypes.map(rt -> convertToDTO(rt, checkIn, checkOut));
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeDTO getRoomTypeById(String id) {
        return roomTypeRepository.findById(id)
                .map(this::convertToDTO)
                .orElseGet(() -> {
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

        if (dto.getGalleryImages() != null && !dto.getGalleryImages().isEmpty()) {
            List<RoomTypeImage> images = dto.getGalleryImages().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> RoomTypeImage.builder().imageUrl(url).roomType(roomType).build())
                    .collect(Collectors.toList());
            roomType.setImages(images);
        }

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

        if (dto.getGalleryImages() != null) {
            roomType.getImages().clear();
            List<RoomTypeImage> newImages = dto.getGalleryImages().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> RoomTypeImage.builder().imageUrl(url).roomType(roomType).build())
                    .collect(Collectors.toList());
            roomType.getImages().addAll(newImages);
        }

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

    private RoomTypeDTO convertToDTO(RoomType roomType) {
        return convertToDTO(roomType, null, null);
    }

    private RoomTypeDTO convertToDTO(RoomType roomType, java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        long availableCount;
        if (checkIn != null && checkOut != null) {
            // 1. Tổng số phòng của loại này (loại trừ MAINTENANCE)
            long totalRooms = roomRepository.countByRoomTypeAndStatusNot(roomType, "MAINTENANCE");
            
            // 2. Tìm tất cả booking trùng lịch có chứa loại phòng này
            java.util.List<com.example.HM.entity.Booking> overlaps = bookingRepository.findOverlappingBookingsByType(roomType.getId(), checkIn, checkOut);
            
            // 3. Tính số lượng bị đặt tối đa trong bất kỳ đêm nào trong khoảng [checkIn, checkOut)
            long maxBooked = 0;
            for (java.time.LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                long dailySum = 0;
                for (com.example.HM.entity.Booking b : overlaps) {
                    if (!date.isBefore(b.getCheckIn()) && date.isBefore(b.getCheckOut())) {
                        // Tính tổng quantity của loại phòng này trong đơn hàng đó
                        long qty = b.getBookedRooms().stream()
                                .filter(br -> br.getRoomType().getId().equals(roomType.getId()))
                                .mapToLong(com.example.HM.entity.BookedRoom::getQuantity)
                                .sum();
                        dailySum += qty;
                    }
                }
                if (dailySum > maxBooked) maxBooked = dailySum;
            }
            
            availableCount = Math.max(0, totalRooms - maxBooked);
        } else {
            // Mặc định tính theo số phòng "AVAILABLE" hiện tại nếu không chọn ngày
            availableCount = roomRepository.countByRoomTypeAndStatus(roomType, "AVAILABLE");
        }
        
        long reviewCount = feedbackRepository.countByRoomType_Id(roomType.getId());
        Double avgRating = feedbackRepository.getAverageRatingByRoomTypeId(roomType.getId());
        
        return RoomTypeDTO.builder()
                .id(roomType.getId())
                .typeName(roomType.getTypeName())
                .description(roomType.getDescription())
                .capacity(roomType.getCapacity())
                .price(roomType.getPrice())
                .roomImage(roomType.getRoomImage())
                .availableCount(availableCount)
                .reviewCount(reviewCount)
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .galleryImages(roomType.getImages() != null ? 
                        roomType.getImages().stream().map(RoomTypeImage::getImageUrl).collect(Collectors.toList()) : 
                        new ArrayList<>())
                .build();
    }
}
