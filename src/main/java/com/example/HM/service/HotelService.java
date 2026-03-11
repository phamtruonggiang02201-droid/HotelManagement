package com.example.HM.service;

import com.example.HM.dto.ServiceCategoryDTO;
import com.example.HM.dto.ServiceDTO;
import com.example.HM.dto.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelService {
    Page<ServiceDTO> getAllServices(Pageable pageable);
    Page<ServiceDTO> searchServices(String keyword, Pageable pageable);
    Page<ServiceCategoryDTO> getAllCategories(Pageable pageable);
    Page<ServiceDTO> getServicesByCategory(String categoryId, Pageable pageable);
    ServiceDTO getServiceById(String id);
    ServiceDTO createService(ServiceRequest request);
    ServiceDTO updateService(String id, ServiceRequest request);
    void deleteService(String id);
    ServiceDTO toggleServiceStatus(String id);
}
