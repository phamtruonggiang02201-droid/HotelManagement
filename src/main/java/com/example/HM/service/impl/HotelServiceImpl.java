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
                        .build());
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
    public void importServicesFromExcel(MultipartFile file) {
        try {
            List<ExtraService> services = ExcelHelper.excelToData(file.getInputStream(), "Services", row -> {
                String serviceName = ExcelHelper.getCellValueAsString(row.getCell(1));
                if (serviceName.isEmpty()) return null;
                
                ExtraService s = new ExtraService();
                s.setServiceName(serviceName);
                
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
            
            // Skip existing names
            services.removeIf(s -> extraServiceRepository.existsByServiceName(s.getServiceName()));
            extraServiceRepository.saveAll(services);
        } catch (IOException e) {
            throw new RuntimeException("Could not store the data: " + e.getMessage());
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
