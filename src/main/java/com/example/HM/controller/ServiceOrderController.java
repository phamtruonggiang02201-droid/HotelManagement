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
}
