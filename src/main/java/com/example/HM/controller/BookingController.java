package com.example.HM.controller;

import com.example.HM.service.AccountService;
import com.example.HM.service.BookingService;
import com.example.HM.service.HotelService;
import com.example.HM.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/admin/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
    public String adminBookingHistory(Model model, @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<com.example.HM.entity.Booking> bookingPage = bookingService.getAllBookings(pageable);
        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", bookingPage.getNumber());
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        return "admin/booking-history";
    }

    @GetMapping("/api/export")
    @ResponseBody
    public ResponseEntity<Resource> exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        String filename = "luxe-stay-bookings.xlsx";
        ByteArrayInputStream in = bookingService.exportBookingsToExcel(startDate, endDate);
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
