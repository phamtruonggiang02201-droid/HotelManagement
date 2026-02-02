package com.example.HM.controller;

import com.example.HM.dto.ServiceCategoryDTO;
import com.example.HM.dto.ServiceDTO;
import com.example.HM.dto.ServiceRequest;
import com.example.HM.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/management/services")
@RequiredArgsConstructor
public class ServiceController {

    private final HotelService hotelService;

    // --- VIEW ROUTE ---

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String serviceIndex() {
        return "service/index";
    }

    // --- API ENDPOINTS ---

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        return ResponseEntity.ok(hotelService.getAllServices());
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<List<ServiceCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(hotelService.getAllCategories());
    }

    @GetMapping("/api/category/{categoryId}")
    @ResponseBody
    public ResponseEntity<List<ServiceDTO>> getServicesByCategory(@PathVariable String categoryId) {
        return ResponseEntity.ok(hotelService.getServicesByCategory(categoryId));
    }

    @PostMapping("/api")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request) {
        try {
            return ResponseEntity.ok(hotelService.createService(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateService(@PathVariable String id, @RequestBody ServiceRequest request) {
        try {
            return ResponseEntity.ok(hotelService.updateService(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteService(@PathVariable String id) {
        hotelService.deleteService(id);
        return ResponseEntity.ok(Map.of("message", "Xóa dịch vụ thành công!"));
    }
}
