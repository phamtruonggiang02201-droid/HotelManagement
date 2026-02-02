package com.example.HM.controller;

import com.example.HM.dto.RoomDTO;
import com.example.HM.dto.RoomRequest;
import com.example.HM.dto.RoomTypeDTO;
import com.example.HM.service.RoomService;
import com.example.HM.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/management")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;

    // --- VIEW ROUTES ---

    @GetMapping("/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
    public String roomIndex() {
        return "room/index";
    }

    @GetMapping("/room-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String roomTypeIndex() {
        return "room-type/index";
    }

    // --- API ENDPOINTS ---

    @GetMapping("/api/rooms")
    @ResponseBody
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/api/room-types")
    @ResponseBody
    public ResponseEntity<List<RoomTypeDTO>> getAllRoomTypes() {
        return ResponseEntity.ok(roomTypeService.getAllRoomTypes());
    }

    @PostMapping("/api/rooms")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest request) {
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
        roomService.deleteRoom(id);
        return ResponseEntity.ok(Map.of("message", "Xóa phòng thành công!"));
    }

    @PostMapping("/api/room-types")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createRoomType(@RequestBody RoomTypeDTO request) {
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
            return ResponseEntity.badRequest().body(Map.of("message", "Không thể xóa loại phòng này vì có thể đang có phòng thuộc loại này!"));
        }
    }
}
