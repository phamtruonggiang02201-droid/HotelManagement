package com.example.HM.service;

import com.example.HM.dto.HeroSliderDTO;
import com.example.HM.dto.HeroSliderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface HeroSliderService {
    Page<HeroSliderDTO> getAllSliders(Pageable pageable);
    Page<HeroSliderDTO> getActiveSliders(Pageable pageable);
    HeroSliderDTO createSlider(HeroSliderRequest request);
    HeroSliderDTO updateSlider(String id, HeroSliderRequest request);
    void deleteSlider(String id);
}
