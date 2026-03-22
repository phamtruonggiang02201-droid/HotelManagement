package com.example.HM.repository;

import com.example.HM.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {
    List<Area> findByParentAreaIsNull(); // Lấy danh sách khu vực cấp cao nhất
    List<Area> findByParentArea_Id(String parentId);
}
