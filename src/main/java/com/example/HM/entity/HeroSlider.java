package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "HeroSliders")
@Getter
@Setter
public class HeroSlider extends BaseEntity {

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @Column(name = "Title", length = 255)
    private String title;

    @Column(name = "Subtitle", length = 500)
    private String subtitle;

    @Column(name = "DisplayOrder")
    private int displayOrder;

    @Column(name = "Active")
    private boolean active = true;
}
