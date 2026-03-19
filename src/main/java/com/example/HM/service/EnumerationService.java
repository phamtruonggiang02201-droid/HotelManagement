package com.example.HM.service;

import com.example.HM.dto.EnumerationDTO;

import java.util.List;

public interface EnumerationService {

    List<EnumerationDTO> getByType(String enumTypeId);

    EnumerationDTO getByTypeAndCode(String enumTypeId, String enumCode);
}
