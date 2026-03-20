package com.example.HM.controller;

import com.example.HM.dto.ServiceCategoryDTO;
import com.example.HM.dto.ServiceDTO;
import com.example.HM.dto.ServiceRequest;
import com.example.HM.service.HotelService;
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
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ServiceController {

    private final HotelService hotelService;

    // --- VIEW ROUTE ---

    @GetMapping("/management/services")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String serviceIndex() {
        return "service/index";
    }

    @GetMapping("/api/services/all")
    @ResponseBody
    public ResponseEntity<List<ServiceDTO>> getAllServicesList() {
        return ResponseEntity.ok(hotelService.getAllServicesList());
    }

    @GetMapping("/api/services/export")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Resource> exportServices() {
        String filename = "luxe-stay-services.xlsx";
        ByteArrayInputStream in = hotelService.exportServicesToExcel();
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/api/services/import")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> importServices(@RequestParam("file") MultipartFile file) {
        try {
            hotelService.importServicesFromExcel(file);
            return ResponseEntity.ok(Map.of("message", "Nhập dữ liệu dịch vụ thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi nhập liệu: " + e.getMessage()));
        }
    }
}
