package com.example.HM.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller này dùng để xử lý các yêu cầu "rác" từ trình duyệt 
 * tránh việc làm bẩn Log của hệ thống.
 */
@RestController
public class SystemNoiseController {

    @GetMapping("/.well-known/**")
    public ResponseEntity<Void> handleWellKnownNoise() {
        return ResponseEntity.notFound().build();
    }
}
