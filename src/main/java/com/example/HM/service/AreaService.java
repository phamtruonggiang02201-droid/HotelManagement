package com.example.HM.service;

import com.example.HM.entity.Area;
import java.util.List;

public interface AreaService {
    List<Area> getAllAreas();
    List<Area> getRootAreas();
    Area getAreaById(String id);
    Area saveArea(Area area);
    void deleteArea(String id);
}
