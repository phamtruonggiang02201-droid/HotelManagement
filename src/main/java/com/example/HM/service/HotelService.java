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
    ByteArrayInputStream exportServicesToExcel();
    void importServicesFromExcel(MultipartFile file);
}
