package com.example.HM.controller;

import com.example.HM.dto.EnumerationDTO;
import com.example.HM.service.EnumerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enumerations")
@RequiredArgsConstructor
public class EnumerationController {

    private final EnumerationService enumerationService;

    @GetMapping("/{typeId}")
    public ResponseEntity<List<EnumerationDTO>> getByType(@PathVariable String typeId) {
        return ResponseEntity.ok(enumerationService.getByType(typeId));
    }

    @GetMapping("/{typeId}/{code}")
    public ResponseEntity<EnumerationDTO> getByTypeAndCode(
            @PathVariable String typeId,
            @PathVariable String code) {
        EnumerationDTO dto = enumerationService.getByTypeAndCode(typeId, code);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}
