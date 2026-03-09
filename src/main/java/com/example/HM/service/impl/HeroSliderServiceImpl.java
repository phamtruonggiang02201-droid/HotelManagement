package com.example.HM.service.impl;

import com.example.HM.dto.HeroSliderDTO;
import com.example.HM.dto.HeroSliderRequest;
import com.example.HM.entity.HeroSlider;
import com.example.HM.repository.HeroSliderRepository;
import com.example.HM.service.HeroSliderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeroSliderServiceImpl implements HeroSliderService {

    private final HeroSliderRepository heroSliderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<HeroSliderDTO> getAllSliders(Pageable pageable) {
        return heroSliderRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HeroSliderDTO> getActiveSliders(Pageable pageable) {
        return heroSliderRepository.findByActiveTrueOrderByDisplayOrderAsc(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public HeroSliderDTO createSlider(HeroSliderRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Tiêu đề không được để trống!");
        }
        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            throw new RuntimeException("URL ảnh không được để trống!");
        }

        HeroSlider slider = new HeroSlider();
        slider.setImageUrl(request.getImageUrl());
        slider.setTitle(request.getTitle());
        slider.setSubtitle(request.getSubtitle());
        slider.setDisplayOrder(request.getDisplayOrder());
        slider.setActive(request.isActive());
        
        return convertToDTO(heroSliderRepository.save(slider));
    }

    @Override
    @Transactional
    public HeroSliderDTO updateSlider(String id, HeroSliderRequest request) {
        HeroSlider slider = heroSliderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slider không tồn tại!"));
        
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Tiêu đề không được để trống!");
        }
        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            throw new RuntimeException("URL ảnh không được để trống!");
        }

        slider.setImageUrl(request.getImageUrl());
        slider.setTitle(request.getTitle());
        slider.setSubtitle(request.getSubtitle());
        slider.setDisplayOrder(request.getDisplayOrder());
        slider.setActive(request.isActive());
        
        return convertToDTO(heroSliderRepository.save(slider));
    }

    @Override
    @Transactional
    public void deleteSlider(String id) {
        heroSliderRepository.deleteById(id);
    }

    private HeroSliderDTO convertToDTO(HeroSlider slider) {
        return HeroSliderDTO.builder()
                .id(slider.getId())
                .imageUrl(slider.getImageUrl())
                .title(slider.getTitle())
                .subtitle(slider.getSubtitle())
                .displayOrder(slider.getDisplayOrder())
                .active(slider.isActive())
                .build();
    }
}
