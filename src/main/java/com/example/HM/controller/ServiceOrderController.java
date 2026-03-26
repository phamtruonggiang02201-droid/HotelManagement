package com.example.HM.controller;

import com.example.HM.dto.BookedServiceDTO;
import com.example.HM.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/service-orders")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
public class ServiceOrderController {

    private final BookingService bookingService;

    @GetMapping
    public String index() {
        return "management/service-order/index";
    }

    @GetMapping("/quick")
    public String quickOrder() {
        return "management/service-order/quick-order";
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Page<BookedServiceDTO>> getAllBookedServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAllBookedServices(keyword, status, pageable));
    }


    @GetMapping("/api/export")
    @ResponseBody
    public ResponseEntity<Resource> exportServiceOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        String filename = "luxe-stay-service-orders.xlsx";
        ByteArrayInputStream in = bookingService.exportBookedServicesToExcel(keyword, status);
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
    @PutMapping("/api/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            return ResponseEntity.ok(bookingService.updateBookedServiceStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/{id}/quantity")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(@PathVariable String id, @RequestBody Map<String, Integer> body) {
        try {
            Integer quantity = body.get("quantity");
            // id ở đây bây giờ là detailId (ID của món cụ thể)
            return ResponseEntity.ok(bookingService.updateBookedServiceQuantity(id, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/occupied-rooms")
    @ResponseBody
    public ResponseEntity<?> getOccupiedRooms() {
        try {
            // Lấy toàn bộ danh sách phòng đang có khách (không giới hạn theo ngày check-out)
            return ResponseEntity.ok(bookingService.getCheckedInBookings(null, null, org.springframework.data.domain.Pageable.unpaged()).getContent());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    @PostMapping("/api/quick")
    @ResponseBody
    public ResponseEntity<?> createQuickOrder(@RequestBody Map<String, Object> request) {
        try {
            String bookingId = (String) request.get("bookingId");
            String roomId = (String) request.get("roomId");
            Map<String, Integer> items = (Map<String, Integer>) request.get("items");
            
            bookingService.bookServices(bookingId, roomId, items);
            return ResponseEntity.ok(Map.of("message", "Đặt dịch vụ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

}
