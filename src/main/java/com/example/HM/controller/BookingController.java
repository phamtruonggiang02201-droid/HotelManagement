package com.example.HM.controller;

import com.example.HM.dto.BookServiceRequest;
import com.example.HM.dto.BookingRequest;
import com.example.HM.dto.CheckInRequest;
import com.example.HM.entity.Booking;
import com.example.HM.security.SecurityUtils;
import com.example.HM.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    private final VNPayService vnPayService;
    private final AccountService accountService;
    private final HotelService hotelService;
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;

    @GetMapping("/edit/{id}")
    public String editBookingForm(@PathVariable String id, Model model) {
        Booking booking = bookingService.getBookingById(id);
        if (booking == null) return "redirect:/bookings/my-bookings";
        
        model.addAttribute("booking", booking);
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes(org.springframework.data.domain.Pageable.unpaged()).getContent());
        return "booking/edit";
    }

    @PostMapping("/api/edit/{id}")
    @ResponseBody
    public ResponseEntity<?> updateBooking(@PathVariable String id, @RequestBody BookingRequest request) {
        try {
            bookingService.updateBooking(id, request);
            return ResponseEntity.ok(Map.of("message", "Cập nhật đặt phòng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/cancel/{id}")
    @ResponseBody
    public ResponseEntity<?> cancelBooking(@PathVariable String id) {
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok(Map.of("message", "Hủy đặt phòng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/new")
    public String bookingForm(
            @RequestParam(required = false) String roomTypeId,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            Model model) {
        
        if ((roomTypeId == null || roomTypeId.isBlank()) && (roomId == null || roomId.isBlank())) {
            return "redirect:/rooms";
        }

        model.addAttribute("roomTypeId", roomTypeId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);

        try {
            if (SecurityUtils.isAuthenticated()) {
                model.addAttribute("account", accountService.getCurrentAccount());
            }
        } catch (Exception e) {
            // Ignore if account not found, guest can still book
        }

        return "booking/new";
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request, HttpServletRequest servletRequest) {
        try {
            String accountId = SecurityUtils.getCurrentUserId();
            Booking booking = bookingService.createBooking(request, accountId);

            String paymentUrl = vnPayService.createPaymentUrl(
                    booking.getTotalAmount().longValue(),
                    "Thanh toan LuxeStay - Booking #" + booking.getId(),
                    booking.getId(),
                    servletRequest
            );

            return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "paymentUrl", paymentUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/walk-in")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
    public ResponseEntity<?> createWalkInBooking(@RequestBody BookingRequest request) {
        try {
            // Nếu có danh sách roomIds thì thực hiện gộp Check-in luôn
            Booking booking;
            if (request.getRoomIds() != null && !request.getRoomIds().isEmpty()) {
                booking = bookingService.createWalkInCheckIn(request);
            } else {
                booking = bookingService.createWalkInBooking(request);
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Đã tiếp nhận đặt phòng thành công!",
                "bookingId", booking.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model, @PageableDefault(size = 10, sort = "checkIn", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        String accountId = SecurityUtils.getCurrentUserId();
        if (accountId == null) {
            return "redirect:/login";
        }
        Page<Booking> bookingPage = bookingService.getMyBookings(accountId, pageable);
        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", bookingPage.getNumber());
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        return "booking/history";
    }

    @GetMapping("/api/my-bookings")
    @ResponseBody
    public ResponseEntity<?> getMyBookingsApi(@PageableDefault(size = 10) Pageable pageable) {
        String accountId = SecurityUtils.getCurrentUserId();
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        return ResponseEntity.ok(bookingService.getMyBookings(accountId, pageable));
    }

    @GetMapping("/{id}")
    public String bookingDetail(@PathVariable String id, Model model) {
        Booking booking = bookingService.getBookingById(id);
        if (booking == null) {
            return "redirect:/bookings/my-bookings";
        }
        model.addAttribute("booking", booking);
        model.addAttribute("services", hotelService.getAllServices(Pageable.unpaged()).getContent());
        model.addAttribute("categories", hotelService.getAllCategories(Pageable.unpaged()).getContent());
        return "booking/detail";
    }

    @PostMapping("/api/book-service")
    @ResponseBody
    public ResponseEntity<?> bookService(@RequestBody BookServiceRequest request) {
        try {
            bookingService.bookService(request);
            return ResponseEntity.ok(Map.of("message", "Thêm dịch vụ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/booked-services/{id}/quantity")
    @ResponseBody
    public ResponseEntity<?> updateBookedServiceQuantity(@PathVariable String id, @RequestBody Map<String, Integer> body) {
        try {
            Integer quantity = body.get("quantity");
            return ResponseEntity.ok(bookingService.updateBookedServiceQuantity(id, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/booked-services/{id}")
    @ResponseBody
    public ResponseEntity<?> cancelBookedService(@PathVariable String id) {
        try {
            return ResponseEntity.ok(bookingService.updateBookedServiceStatus(id, "CANCELLED"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/booked-services/{id}/rate")
    @ResponseBody
    public ResponseEntity<?> rateBookedService(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        try {
            int rating = (int) payload.get("rating");
            String comment = (String) payload.get("comment");
            bookingService.rateBookedDetail(id, rating, comment);
            return ResponseEntity.ok(Map.of("message", "Đánh giá thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ============ RECEPTION: CHECK-IN / CHECK-OUT ============

    @GetMapping("/reception")
    @PreAuthorize("hasRole('RECEPTION')")
    public String receptionDashboard(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @RequestParam(required = false) String keyword,
            Model model, 
            @org.springframework.beans.factory.annotation.Qualifier("paid") @PageableDefault(size = 5, sort = "checkIn", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable paidPageable,
            @org.springframework.beans.factory.annotation.Qualifier("checked") @PageableDefault(size = 5, sort = "checkIn", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable checkedPageable) {

        LocalDate selectedDate = date != null ? date : LocalDate.now();
        boolean isTodayFilter = LocalDate.now().equals(selectedDate);

        model.addAttribute("paidBookings", isTodayFilter
                ? bookingService.getPaidBookings(selectedDate, keyword, paidPageable)
                : bookingService.getReceptionCheckInBookings(selectedDate, keyword, paidPageable));
        model.addAttribute("checkedInBookings", isTodayFilter
                ? bookingService.getCheckedInBookings(selectedDate, keyword, checkedPageable)
                : bookingService.getReceptionCheckOutBookings(selectedDate, keyword, checkedPageable));
        model.addAttribute("isTodayFilter", isTodayFilter);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("keyword", keyword);
        model.addAttribute("services", hotelService.getAllServices(Pageable.unpaged()).getContent());
        model.addAttribute("categories", hotelService.getAllCategories(Pageable.unpaged()).getContent());
        
        return "booking/reception";
    }

    @GetMapping("/walk-in")
    @PreAuthorize("hasRole('RECEPTION')")
    public String walkInBookingPage(Model model) {
        return "booking/walk-in";
    }

    @GetMapping("/check-in/{bookingId}")
    @PreAuthorize("hasRole('RECEPTION')")
    public String checkInForm(@PathVariable String bookingId, Model model) {
        model.addAttribute("checkInData", bookingService.getCheckInData(bookingId));
        return "booking/checkin";
    }

    @GetMapping("/check-in/{bookingId}/guests")
    @PreAuthorize("hasRole('RECEPTION')")
    public String checkInGuestsForm(@PathVariable String bookingId, Model model) {
        model.addAttribute("bookingId", bookingId);
        return "booking/checkin_guests";
    }

    @PostMapping("/api/check-in")
    @ResponseBody
    @PreAuthorize("hasRole('RECEPTION')")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest request) {
        try {
            bookingService.checkIn(request);
            return ResponseEntity.ok(Map.of("message", "Check-in thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/check-out/{bookingId}")
    @ResponseBody
    @PreAuthorize("hasRole('RECEPTION')")
    public ResponseEntity<?> checkOut(@PathVariable String bookingId) {
        try {
            bookingService.checkOut(bookingId);
            return ResponseEntity.ok(Map.of("message", "Check-out thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/available-rooms/{roomTypeId}")
    @ResponseBody
    @PreAuthorize("hasRole('RECEPTION')")
    public ResponseEntity<?> getAvailableRooms(@PathVariable String roomTypeId, @PageableDefault(size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(roomService.getRoomsByRoomTypeAndStatus(roomTypeId, "AVAILABLE", pageable));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/api/checkout-summary/{bookingId}")
    @ResponseBody
    @PreAuthorize("hasRole('RECEPTION')")
    public ResponseEntity<?> getCheckoutSummary(@PathVariable String bookingId) {
        try {
            return ResponseEntity.ok(bookingService.getCheckoutSummary(bookingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTION')")
    public String adminBookingHistory(Model model, @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<com.example.HM.entity.Booking> bookingPage = bookingService.getAllBookings(pageable);
        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", bookingPage.getNumber());
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        return "admin/booking-history";
    }

    @GetMapping("/invoice/{bookingId}")
    @PreAuthorize("hasRole('RECEPTION')")
    public String showInvoice(@PathVariable String bookingId, Model model) {
        model.addAttribute("summary", bookingService.getCheckoutSummary(bookingId));
        model.addAttribute("booking", bookingService.getBookingById(bookingId));
        return "booking/invoice";
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
