package com.example.HM.service.impl;

import com.example.HM.dto.EnumerationDTO;
import com.example.HM.entity.Enumeration;
import com.example.HM.repository.EnumerationRepository;
import com.example.HM.service.EnumerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnumerationServiceImpl implements EnumerationService {

    private final EnumerationRepository enumerationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EnumerationDTO> getByType(String enumTypeId) {
        return enumerationRepository.findByEnumType_EnumTypeIdOrderBySequenceNumAsc(enumTypeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnumerationDTO getByTypeAndCode(String enumTypeId, String enumCode) {
        Enumeration enumeration = enumerationRepository
                .findByEnumType_EnumTypeIdAndEnumCode(enumTypeId, enumCode)
                .orElse(null);
        return enumeration != null ? convertToDTO(enumeration) : null;
    }

    private EnumerationDTO convertToDTO(Enumeration enumeration) {
        return EnumerationDTO.builder()
                .id(enumeration.getId())
                .enumCode(enumeration.getEnumCode())
                .enumName(enumeration.getEnumName())
                .enumTypeId(enumeration.getEnumType() != null ? enumeration.getEnumType().getEnumTypeId() : null)
                .sequenceNum(enumeration.getSequenceNum())
                .description(enumeration.getDescription())
                .build();
    }
}
