package com.example.HM.dto;

import lombok.Data;

@Data
public class HeroSliderRequest {
    private String imageUrl;
    private String title;
    private String subtitle;
    private int displayOrder;
    private boolean active;
}
