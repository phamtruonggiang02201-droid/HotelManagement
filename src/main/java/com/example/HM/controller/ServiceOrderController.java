package com.example.HM.controller;

import com.example.HM.dto.BookedServiceDTO;
import com.example.HM.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Page<BookedServiceDTO>> getAllBookedServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAllBookedServices(keyword, status, pageable));
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
}
