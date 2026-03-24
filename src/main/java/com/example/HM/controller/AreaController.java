package com.example.HM.controller;

import com.example.HM.entity.Area;
import com.example.HM.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    public List<Area> getAllAreas() {
        return areaService.getAllAreas();
    }

    @GetMapping("/roots")
    public List<Area> getRootAreas() {
        return areaService.getRootAreas();
    }

    @GetMapping("/{id}")
    public Area getAreaById(@PathVariable String id) {
        return areaService.getAreaById(id);
    }

    @PostMapping
    public Area saveArea(@RequestBody Area area) {
        return areaService.saveArea(area);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArea(@PathVariable String id, @RequestParam(defaultValue = "false") boolean force) {
        try {
            areaService.deleteArea(id, force);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
