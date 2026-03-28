package com.example.HM.service;

import com.example.HM.dto.ServiceCategoryDTO;
import com.example.HM.dto.ServiceDTO;
import com.example.HM.dto.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface HotelService {
    List<ServiceDTO> getAllServicesList();
    List<ServiceCategoryDTO> getAllCategoriesList();
    
    Page<ServiceDTO> getAllServices(Pageable pageable);
    Page<ServiceCategoryDTO> getAllCategories(Pageable pageable);
    Page<ServiceDTO> searchServices(String keyword, Pageable pageable);
    Page<ServiceDTO> getServicesByCategory(String categoryId, Pageable pageable);
    ServiceDTO getServiceById(String id);
    ServiceDTO createService(ServiceRequest request);
    ServiceDTO updateService(String id, ServiceRequest request);
    void deleteService(String id);
    ServiceDTO toggleServiceStatus(String id);

    // Excel Operations
    ByteArrayInputStream exportServicesToExcel();
    String importServicesFromExcel(MultipartFile file);
}
