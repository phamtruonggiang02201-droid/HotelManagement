package com.example.HM.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Area extends BaseEntity {

    @Column(name = "area_name", length = 100, nullable = false)
    private String areaName;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_area_id")
    @JsonBackReference
    private Area parentArea;

    @OneToMany(mappedBy = "parentArea", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Area> subAreas;

    @OneToMany(mappedBy = "area")
    @JsonIgnore
    private List<Room> rooms;
}
