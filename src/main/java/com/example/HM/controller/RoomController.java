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
    private final com.example.HM.service.FeedbackService feedbackService;

    // --- VIEW ROUTES ---

    @GetMapping("/rooms")
    public String guestRoomList() {
        return "room/list";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@PathVariable String id, org.springframework.ui.Model model) {
        model.addAttribute("roomTypeId", id);
        return "room/detail";
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

    @GetMapping("/management/areas")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String areaIndex() {
        return "management/area";
    }

    @GetMapping("/management/room-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String roomTypeIndex() {
        return "room-type/index";
    }

    @GetMapping("/management/room-types/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String roomTypeCreateForm(org.springframework.ui.Model model) {
        model.addAttribute("mode", "create");
        return "room-type/detail";
    }

    @GetMapping("/management/room-types/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String roomTypeEditForm(@PathVariable String id, org.springframework.ui.Model model) {
        model.addAttribute("mode", "edit");
        model.addAttribute("roomTypeId", id);
        return "room-type/detail";
    }

    @GetMapping("/management/room-types/{id}/detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String roomTypeDetailForm(@PathVariable String id, org.springframework.ui.Model model) {
        model.addAttribute("mode", "view");
        model.addAttribute("roomTypeId", id);
        return "room-type/detail";
    }

    // --- API ENDPOINTS ---

    @GetMapping("/api/rooms")
    @ResponseBody
    public ResponseEntity<Page<RoomDTO>> getAllRooms(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(roomService.getAllRooms(pageable));
    }

    @GetMapping("/api/rooms/status-by-date")
    @ResponseBody
    public ResponseEntity<List<RoomDTO>> getRoomStatusByDate(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(roomService.getRoomStatusByDate(date));
    }

    @GetMapping("/api/rooms/{id}")
    @ResponseBody
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable String id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/api/rooms/stats")
    @ResponseBody
    public ResponseEntity<com.example.HM.dto.RoomStatsDTO> getRoomStats(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(roomService.getRoomStatsByDate(date));
    }

    @GetMapping("/api/rooms/search")
    @ResponseBody
    public ResponseEntity<Page<RoomDTO>> searchRooms(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String typeId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @RequestParam(required = false) String areaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("roomName").ascending());
        return ResponseEntity.ok(roomService.searchRooms(keyword, typeId, minPrice, maxPrice, date, areaId, pageable));
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
            @RequestParam(required = false) String typeId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkIn,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkOut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("typeName").ascending());
        return ResponseEntity.ok(roomTypeService.searchRoomTypes(keyword, typeId, checkIn, checkOut, pageable));
    }

    @GetMapping("/api/room-types/{id}/feedbacks")
    @ResponseBody
    public ResponseEntity<Page<com.example.HM.entity.Feedback>> getRoomTypeFeedbacks(
            @PathVariable String id,
            @PageableDefault(size = 5) Pageable pageable) {
        // Ta sử dụng Page trực tiếp nhưng Jackson sẽ xử lý nhờ các @JsonIgnore đã có.
        // Để an tâm tuyệt đối, ta có thể dùng DTO nhưng hiện tại đã fix Occupant.
        return ResponseEntity.ok(feedbackService.getFeedbacksByRoomType(id, pageable));
    }

    @GetMapping("/api/room-types/{id}/rooms")
    @ResponseBody
    public ResponseEntity<List<RoomDTO>> getRoomsByRoomType(@PathVariable String id,
                                                         @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkIn,
                                                         @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate checkOut) {
        if (checkIn != null && checkOut != null) {
            return ResponseEntity.ok(roomService.getAvailableRoomsByType(id, checkIn, checkOut));
        }
        Page<RoomDTO> rooms = roomService.getRoomsByRoomTypeAndStatus(id, "AVAILABLE", Pageable.unpaged());
        return ResponseEntity.ok(rooms.getContent());
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

    // --- EXCEL ENDPOINTS ---

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
            String result = roomTypeService.importRoomTypesFromExcel(file);
            return ResponseEntity.ok(Map.of("message", result));
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
            String result = roomService.importRoomsFromExcel(file);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi nhập liệu: " + e.getMessage()));
        }
    }
    @PostMapping("/api/rooms/assign-area")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignDefaultArea() {
        try {
            roomService.assignDefaultAreaToOldRooms();
            return ResponseEntity.ok(Map.of("message", "Đã gán khu vực mặc định cho các phòng cũ!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
