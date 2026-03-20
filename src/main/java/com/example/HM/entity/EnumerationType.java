package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "EnumerationType")
@Getter
@Setter
public class EnumerationType extends BaseEntity {

    @Column(name = "enum_type_id", length = 50, nullable = false, unique = true)
    private String enumTypeId;

    @Column(name = "description", length = 255)
    private String description;
}
