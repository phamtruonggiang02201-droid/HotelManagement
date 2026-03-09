package com.example.HM.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeroSliderDTO {
    private String id;
    private String imageUrl;
    private String title;
    private String subtitle;
    private int displayOrder;
    private boolean active;
}
