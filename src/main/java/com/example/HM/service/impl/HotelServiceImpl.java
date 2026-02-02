package com.example.HM.service.impl;

import com.example.HM.dto.ServiceCategoryDTO;
import com.example.HM.dto.ServiceDTO;
import com.example.HM.dto.ServiceRequest;
import com.example.HM.entity.Service;
import com.example.HM.entity.ServiceCategory;
import com.example.HM.repository.ServiceCategoryRepository;
import com.example.HM.repository.ServiceRepository;
import com.example.HM.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategoryDTO> getAllCategories() {
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
    public List<ServiceDTO> getServicesByCategory(String categoryId) {
        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
        return serviceRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDTO getServiceById(String id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));
        return convertToDTO(service);
    }

    @Override
    @Transactional
    public ServiceDTO createService(ServiceRequest request) {
        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không hợp lệ!"));
        
        Service service = new Service();
        service.setServiceName(request.getServiceName());
        service.setPrice(request.getPrice());
        service.setCategory(category);
        
        return convertToDTO(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public ServiceDTO updateService(String id, ServiceRequest request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));
        
        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không hợp lệ!"));
                
        service.setServiceName(request.getServiceName());
        service.setPrice(request.getPrice());
        service.setCategory(category);
        
        return convertToDTO(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public void deleteService(String id) {
        serviceRepository.deleteById(id);
    }

    private ServiceDTO convertToDTO(Service service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .serviceName(service.getServiceName())
                .price(service.getPrice())
                .categoryName(service.getCategory() != null ? service.getCategory().getCategoryName() : null)
                .build();
    }
}
