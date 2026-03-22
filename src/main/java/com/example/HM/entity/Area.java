package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "Areas")
@Getter
@Setter
@RequiredArgsConstructor
public class Area extends BaseEntity {

    @Column(name = "area_name", length = 100, nullable = false)
    private String areaName;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_area_id")
    private Area parentArea;

    @OneToMany(mappedBy = "parentArea", cascade = CascadeType.ALL)
    private List<Area> subAreas;

    @OneToMany(mappedBy = "area")
    private List<Room> rooms;
}
