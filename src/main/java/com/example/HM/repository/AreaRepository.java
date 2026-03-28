package com.example.HM.repository;

import com.example.HM.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {
    List<Area> findByParentAreaIsNullOrderByAreaNameAsc(); // Lấy danh sách khu vực cấp cao nhất, sắp xếp theo tên
    List<Area> findByParentArea_IdOrderByAreaNameAsc(String parentId);
    List<Area> findAllByOrderByAreaNameAsc();
}
