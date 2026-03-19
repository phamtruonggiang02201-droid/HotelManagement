package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "RoomType")
@Getter
@Setter
public class RoomType extends BaseEntity {

    @Column(name = "TypeName", length = 100)
    private String typeName;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "Capacity")
    private Integer capacity;

    @Column(name = "Price", precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "RoomImage", length = 255)
    private String roomImage;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoomTypeImage> images = new ArrayList<>();
}
