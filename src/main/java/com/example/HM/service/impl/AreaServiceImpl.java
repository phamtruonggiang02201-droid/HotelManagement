package com.example.HM.service.impl;

import com.example.HM.entity.Area;
import com.example.HM.repository.AreaRepository;
import com.example.HM.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final AreaRepository areaRepository;

    @Override
    public List<Area> getAllAreas() {
        return areaRepository.findAll();
    }

    @Override
    public List<Area> getRootAreas() {
        return areaRepository.findByParentAreaIsNull();
    }

    @Override
    public Area getAreaById(String id) {
        return areaRepository.findById(id).orElseThrow(() -> new RuntimeException("Khu vực không tồn tại!"));
    }

    @Override
    @Transactional
    public Area saveArea(Area area) {
        if (area.getAreaName() != null) {
            area.setAreaName(area.getAreaName().trim());
        }
        return areaRepository.save(area);
    }

    @Override
    @Transactional
    public void deleteArea(String id) {
        Area area = getAreaById(id);
        if (area.getRooms() != null && !area.getRooms().isEmpty()) {
            throw new RuntimeException("Không thể xóa khu vực đang có phòng!");
        }
        areaRepository.deleteById(id);
    }
}
