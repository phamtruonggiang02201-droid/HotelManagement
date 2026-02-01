package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
}
