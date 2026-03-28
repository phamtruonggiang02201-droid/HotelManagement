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
    private final com.example.HM.repository.WorkAssignmentRepository workAssignmentRepository;

    @Override
    public List<Area> getAllAreas() {
        return areaRepository.findAllByOrderByAreaNameAsc();
    }

    @Override
    public List<Area> getRootAreas() {
        return areaRepository.findByParentAreaIsNullOrderByAreaNameAsc();
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
        deleteArea(id, false);
    }

    @Transactional
    public void deleteArea(String id, boolean force) {
        Area area = getAreaById(id);
        if (area.getRooms() != null && !area.getRooms().isEmpty()) {
            throw new RuntimeException("Không thể xóa khu vực đang có phòng!");
        }

        List<com.example.HM.entity.WorkAssignment> assignments = workAssignmentRepository
                .findByAreaAndWorkDateGreaterThanEqual(area.getAreaName(), java.time.LocalDate.now());

        if (!assignments.isEmpty() && !force) {
            StringBuilder sb = new StringBuilder("Cảnh báo: Đang có phân công công việc trong tương lai tại khu vực này:\n");
            for (com.example.HM.entity.WorkAssignment wa : assignments) {
                String empName = wa.getEmployee() != null ? wa.getEmployee().getFullName() : "Chưa gán";
                sb.append("- Ngày ").append(wa.getWorkDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")))
                  .append(" đang phân công cho ").append(empName).append("\n");
            }
            sb.append("Đại ca có chắc chắn muốn xóa và HỦY toàn bộ lịch trình này không?");
            throw new RuntimeException(sb.toString());
        }

        if (force) {
            workAssignmentRepository.deleteByAreaAndWorkDateGreaterThanEqual(area.getAreaName(), java.time.LocalDate.now());
        }

        areaRepository.deleteById(id);
    }
}
