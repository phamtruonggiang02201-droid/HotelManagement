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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    // --- API ENDPOINTS ---

    @GetMapping("/api/services")
    @ResponseBody
    public ResponseEntity<Page<ServiceDTO>> getAllServices(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(hotelService.getAllServices(pageable));
    }

    @GetMapping("/api/services/search")
    @ResponseBody
    public ResponseEntity<Page<ServiceDTO>> searchServices(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("serviceName").ascending());
        return ResponseEntity.ok(hotelService.searchServices(keyword, pageable));
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<Page<ServiceCategoryDTO>> getAllCategories(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(hotelService.getAllCategories(pageable));
    }

    @GetMapping("/api/services/category/{categoryId}")
    @ResponseBody
    public ResponseEntity<Page<ServiceDTO>> getServicesByCategory(@PathVariable String categoryId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(hotelService.getServicesByCategory(categoryId, pageable));
    }

    @PostMapping("/api/services")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request) {
        try {
            return ResponseEntity.ok(hotelService.createService(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/services/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateService(@PathVariable String id, @RequestBody ServiceRequest request) {
        try {
            return ResponseEntity.ok(hotelService.updateService(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/services/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteService(@PathVariable String id) {
        hotelService.deleteService(id);
        return ResponseEntity.ok(Map.of("message", "Xóa dịch vụ thành công!"));
    }

    @PutMapping("/api/services/{id}/toggle-status")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> toggleServiceStatus(@PathVariable String id) {
        try {
            return ResponseEntity.ok(hotelService.toggleServiceStatus(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
