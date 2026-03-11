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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;

    // --- VIEW ROUTES ---

    @GetMapping("/rooms")
    public String guestRoomList() {
        return "room/list";
    }

    @GetMapping("/reception/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
    public String receptionistRoomStatus() {
        return "room/status";
    }

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

    // --- API ENDPOINTS ---

    @GetMapping("/api/rooms")
    @ResponseBody
    public ResponseEntity<Page<RoomDTO>> getAllRooms(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(roomService.getAllRooms(pageable));
    }

    @GetMapping("/api/rooms/search")
    @ResponseBody
    public ResponseEntity<Page<RoomDTO>> searchRooms(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String typeId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("roomName").ascending());
        return ResponseEntity.ok(roomService.searchRooms(keyword, typeId, minPrice, maxPrice, pageable));
    }

    @GetMapping("/api/room-types")
    @ResponseBody
    public ResponseEntity<Page<RoomTypeDTO>> getAllRoomTypes(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(roomTypeService.getAllRoomTypes(pageable));
    }

    @GetMapping("/api/room-types/{id}")
    @ResponseBody
    public ResponseEntity<RoomTypeDTO> getRoomTypeById(@PathVariable String id) {
        return ResponseEntity.ok(roomTypeService.getRoomTypeById(id));
    }

    @GetMapping("/api/room-types/search")
    @ResponseBody
    public ResponseEntity<Page<RoomTypeDTO>> searchRoomTypes(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("typeName").ascending());
        return ResponseEntity.ok(roomTypeService.searchRoomTypes(keyword, pageable));
    }

    @PostMapping("/api/rooms")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomRequest request) {
        try {
            return ResponseEntity.ok(roomService.createRoom(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/rooms/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateRoom(@PathVariable String id, @RequestBody RoomRequest request) {
        try {
            return ResponseEntity.ok(roomService.updateRoom(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/rooms/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteRoom(@PathVariable String id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok(Map.of("message", "Xóa phòng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/room-types")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createRoomType(@Valid @RequestBody RoomTypeDTO request) {
        try {
            return ResponseEntity.ok(roomTypeService.createRoomType(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/room-types/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateRoomType(@PathVariable String id, @RequestBody RoomTypeDTO request) {
        try {
            return ResponseEntity.ok(roomTypeService.updateRoomType(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/room-types/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteRoomType(@PathVariable String id) {
        try {
            roomTypeService.deleteRoomType(id);
            return ResponseEntity.ok(Map.of("message", "Xóa loại phòng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
