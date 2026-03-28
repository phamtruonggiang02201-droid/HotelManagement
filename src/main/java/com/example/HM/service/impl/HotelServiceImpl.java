package com.example.HM.service.impl;

import com.example.HM.dto.ServiceCategoryDTO;
import com.example.HM.dto.ServiceDTO;
import com.example.HM.dto.ServiceRequest;
import com.example.HM.entity.ExtraService;
import com.example.HM.entity.ServiceCategory;
import com.example.HM.repository.ExtraServiceRepository;
import com.example.HM.repository.ServiceCategoryRepository;
import com.example.HM.service.HotelService;
import com.example.HM.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ExtraServiceRepository extraServiceRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServicesList() {
        return extraServiceRepository.findAll().stream()
                .filter(s -> s.getIsActive() != null ? s.getIsActive() : true)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategoryDTO> getAllCategoriesList() {
        return serviceCategoryRepository.findAll().stream()
                .map(cat -> ServiceCategoryDTO.builder()
                        .id(cat.getId())
                        .categoryName(cat.getCategoryName())
                        .description(cat.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ServiceDTO> getAllServices(Pageable pageable) {
        return extraServiceRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceCategoryDTO> getAllCategories(Pageable pageable) {
        return serviceCategoryRepository.findAll(pageable)
                .map(cat -> ServiceCategoryDTO.builder()
                        .id(cat.getId())
                        .categoryName(cat.getCategoryName())
                        .description(cat.getDescription())
                        .build());
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ServiceDTO> searchServices(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return extraServiceRepository.findAll(pageable).map(this::convertToDTO);
        }
        return extraServiceRepository.findByServiceNameContainingIgnoreCase(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceDTO> getServicesByCategory(String categoryId, Pageable pageable) {
        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
        // Chỉ lấy dịch vụ đang hoạt động cho khách
        return extraServiceRepository.findByCategoryAndIsActiveTrue(category, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDTO getServiceById(String id) {
        ExtraService service = extraServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));
        return convertToDTO(service);
    }

    @Override
    @Transactional
    public ServiceDTO createService(ServiceRequest request) {
        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        ExtraService service = new ExtraService();
        service.setServiceName(request.getServiceName());
        service.setPrice(request.getPrice());
        service.setCategory(category);
        service.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return convertToDTO(extraServiceRepository.save(service));
    }

    @Override
    @Transactional
    public ServiceDTO updateService(String id, ServiceRequest request) {
        ExtraService service = extraServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));

        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        service.setServiceName(request.getServiceName());
        service.setPrice(request.getPrice());
        service.setCategory(category);
        service.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return convertToDTO(extraServiceRepository.save(service));
    }

    @Override
    @Transactional
    public void deleteService(String id) {
        ExtraService service = extraServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));
        try {
            extraServiceRepository.delete(service);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa dịch vụ vì đang được sử dụng trong các đơn đặt phòng!");
        }
    }

    @Override
    @Transactional
    public ServiceDTO toggleServiceStatus(String id) {
        ExtraService service = extraServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));
        // Xử lý null-safe: Nếu null thì coi như đang active (true), đảo ngược thành false
        boolean currentStatus = service.getIsActive() != null ? service.getIsActive() : true;
        service.setIsActive(!currentStatus);
        return convertToDTO(extraServiceRepository.save(service));
    }

    @Override
    public ByteArrayInputStream exportServicesToExcel() {
        List<ExtraService> services = extraServiceRepository.findAll();
        String[] headers = { "ID", "Tên dịch vụ", "Danh mục", "Giá", "Trạng thái" };
        
        return ExcelHelper.dataToExcel(services, "Services", headers, (row, s) -> {
            row.createCell(0).setCellValue(s.getId());
            row.createCell(1).setCellValue(s.getServiceName());
            row.createCell(2).setCellValue(s.getCategory() != null ? s.getCategory().getCategoryName() : "N/A");
            row.createCell(3).setCellValue(s.getPrice() != null ? s.getPrice().doubleValue() : 0.0);
            row.createCell(4).setCellValue(s.getIsActive() != null && s.getIsActive() ? "Hoạt động" : "Ngừng");
        });
    }

    @Override
    @Transactional
    public String importServicesFromExcel(MultipartFile file) {
        try {
            List<ExtraService> allServices = ExcelHelper.excelToData(file.getInputStream(), "Services", row -> {
                String serviceName = ExcelHelper.getCellValueAsString(row.getCell(1));
                if (serviceName == null || serviceName.trim().isEmpty()) return null;
                
                ExtraService s = new ExtraService();
                s.setServiceName(serviceName.trim());
                
                String categoryName = ExcelHelper.getCellValueAsString(row.getCell(2));
                ServiceCategory cat = serviceCategoryRepository.findAll().stream()
                        .filter(c -> c.getCategoryName().equalsIgnoreCase(categoryName))
                        .findFirst()
                        .orElse(null);
                s.setCategory(cat);
                
                s.setPrice(new BigDecimal(ExcelHelper.getCellValueAsString(row.getCell(3))));
                s.setIsActive(ExcelHelper.getCellValueAsString(row.getCell(4)).equalsIgnoreCase("Hoạt động"));
                return s;
            });

            int duplicateCount = 0;
            List<ExtraService> toSave = new java.util.ArrayList<>();
            for (ExtraService s : allServices) {
                if (extraServiceRepository.existsByServiceName(s.getServiceName())) {
                    duplicateCount++;
                } else {
                    toSave.add(s);
                }
            }
            
            extraServiceRepository.saveAll(toSave);
            return String.format("Nhập dữ liệu thành công! Đã thêm %d dịch vụ mới. Bỏ qua %d dịch vụ do trùng tên.", 
                    toSave.size(), duplicateCount);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }
    }

    private ServiceDTO convertToDTO(ExtraService service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .serviceName(service.getServiceName())
                .price(service.getPrice())
                .categoryId(service.getCategory() != null ? service.getCategory().getId() : null)
                .categoryName(service.getCategory() != null ? service.getCategory().getCategoryName() : null)
                .isActive(service.getIsActive() != null ? service.getIsActive() : true)
                .build();
    }
}
