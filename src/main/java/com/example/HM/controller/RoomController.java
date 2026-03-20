package com.example.HM.controller;

import com.example.HM.dto.RoomDTO;
import com.example.HM.dto.RoomRequest;
import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.service.RoomService;
import com.example.HM.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;

    @GetMapping("/management/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String roomIndex() {
        return "room/index";
    }

    @GetMapping("/management/room-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String roomTypeIndex() {
        return "room-type/index";
    }


    @GetMapping("/api/room-types/export")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Resource> exportRoomTypes() {
        String filename = "luxe-stay-room-types.xlsx";
        ByteArrayInputStream in = roomTypeService.exportRoomTypesToExcel();
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/api/room-types/import")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> importRoomTypes(@RequestParam("file") MultipartFile file) {
        try {
            roomTypeService.importRoomTypesFromExcel(file);
            return ResponseEntity.ok(Map.of("message", "Nhập dữ liệu loại phòng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi nhập liệu: " + e.getMessage()));
        }
    }

    @GetMapping("/api/rooms/export")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Resource> exportRooms() {
        String filename = "luxe-stay-rooms.xlsx";
        ByteArrayInputStream in = roomService.exportRoomsToExcel();
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/api/rooms/import")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> importRooms(@RequestParam("file") MultipartFile file) {
        try {
            roomService.importRoomsFromExcel(file);
            return ResponseEntity.ok(Map.of("message", "Nhập dữ liệu phòng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi nhập liệu: " + e.getMessage()));
        }
    }
}
