package com.example.HM.controller;

import com.example.HM.dto.BookServiceRequest;
import com.example.HM.entity.Booking;
import com.example.HM.service.BookingService;
import com.example.HM.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/services/quick-order")
public class GuestServiceController {

    private final BookingService bookingService;
    private final HotelService hotelService;

    @GetMapping
    public String guestQuickOrder(Model model) {
        // Cung cấp danh mục để render tabs ban đầu
        model.addAttribute("categories", hotelService.getAllCategoriesList());
        return "guest/quick-booking";
    }

    // --- PUBLIC APIs ---

    @GetMapping("/api/occupied-rooms")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getOccupiedRooms() {
        // Chỉ trả về thông tin tối thiểu để bảo mật: ID Booking và Tên Phòng
        List<Map<String, Object>> rooms = bookingService.getCheckedInBookings(null, null, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .map(b -> Map.<String, Object>of(
                        "bookingId", b.getId(),
                        "roomName", b.getRooms().stream().map(r -> r.getRoomName()).collect(Collectors.joining(", ")),
                        "guestName", b.getGuest().getFullName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/api/book")
    @ResponseBody
    public ResponseEntity<?> bookServices(@RequestBody Map<String, Object> payload) {
        try {
            String bookingId = (String) payload.get("bookingId");
            // items format: { "serviceId": quantity }
            Map<String, Integer> items = (Map<String, Integer>) payload.get("items");
            
            bookingService.bookServices(bookingId, null, items);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi yêu cầu phục vụ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
