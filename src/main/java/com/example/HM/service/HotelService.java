package com.example.HM.service;

import com.example.HM.dto.ServiceCategoryDTO;
import com.example.HM.dto.ServiceDTO;
import com.example.HM.dto.ServiceRequest;
import java.util.List;

public interface HotelService {
    List<ServiceDTO> getAllServices();
    List<ServiceCategoryDTO> getAllCategories();
    List<ServiceDTO> getServicesByCategory(String categoryId);
    ServiceDTO getServiceById(String id);
    ServiceDTO createService(ServiceRequest request);
    ServiceDTO updateService(String id, ServiceRequest request);
    void deleteService(String id);
}
