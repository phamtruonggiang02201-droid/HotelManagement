package com.example.HM.controller;

import com.example.HM.dto.HeroSliderDTO;
import com.example.HM.dto.HeroSliderRequest;
import com.example.HM.service.HeroSliderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HeroSliderController {

    private final HeroSliderService heroSliderService;

    // --- VIEW ROUTE ---

    @GetMapping("/management/hero-slider")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String sliderIndex() {
        return "management/hero-slider/index";
    }

    // --- API ENDPOINTS ---

    @GetMapping("/api/sliders")
    @ResponseBody
    public ResponseEntity<Page<HeroSliderDTO>> getActiveSliders(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(heroSliderService.getActiveSliders(pageable));
    }

    @GetMapping("/api/management/sliders")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<HeroSliderDTO>> getAllSliders(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(heroSliderService.getAllSliders(pageable));
    }

    @PostMapping("/api/management/sliders")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createSlider(@RequestBody HeroSliderRequest request) {
        try {
            return ResponseEntity.ok(heroSliderService.createSlider(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/management/sliders/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateSlider(@PathVariable String id, @RequestBody HeroSliderRequest request) {
        try {
            return ResponseEntity.ok(heroSliderService.updateSlider(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/management/sliders/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteSlider(@PathVariable String id) {
        try {
            heroSliderService.deleteSlider(id);
            return ResponseEntity.ok(Map.of("message", "Xóa slider thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
