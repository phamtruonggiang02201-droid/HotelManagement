package com.example.HM.service.impl;

import com.example.HM.dto.ServiceCategoryDTO;
import com.example.HM.dto.ServiceDTO;
import com.example.HM.dto.ServiceRequest;
import com.example.HM.entity.ExtraService;
import com.example.HM.entity.ServiceCategory;
import com.example.HM.repository.ServiceCategoryRepository;
import com.example.HM.repository.ExtraServiceRepository;
import com.example.HM.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final ExtraServiceRepository extraServiceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceDTO> getAllServices(Pageable pageable) {
        return extraServiceRepository.findAll(pageable)
                .map(this::convertToDTO);
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
        // Validate
        if (request.getServiceName() == null || request.getServiceName().isBlank()) {
            throw new RuntimeException("Tên dịch vụ không được để trống!");
        }
        if (extraServiceRepository.existsByServiceName(request.getServiceName())) {
            throw new RuntimeException("Tên dịch vụ đã tồn tại!");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Giá dịch vụ không được âm!");
        }

        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không hợp lệ!"));
        
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

        // Validate
        if (request.getServiceName() == null || request.getServiceName().isBlank()) {
            throw new RuntimeException("Tên dịch vụ không được để trống!");
        }
        if (extraServiceRepository.existsByServiceNameAndIdNot(request.getServiceName(), id)) {
            throw new RuntimeException("Tên dịch vụ đã tồn tại!");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Giá dịch vụ không được âm!");
        }
        
        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không hợp lệ!"));
                
        service.setServiceName(request.getServiceName());
        service.setPrice(request.getPrice());
        service.setCategory(category);
        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }
        
        return convertToDTO(extraServiceRepository.save(service));
    }

    @Override
    @Transactional
    public void deleteService(String id) {
        extraServiceRepository.deleteById(id);
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
