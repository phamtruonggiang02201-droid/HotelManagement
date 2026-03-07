package com.example.HM.repository;

import com.example.HM.entity.HeroSlider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface HeroSliderRepository extends JpaRepository<HeroSlider, String> {
    Page<HeroSlider> findByActiveTrueOrderByDisplayOrderAsc(Pageable pageable);
}
