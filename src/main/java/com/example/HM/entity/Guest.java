package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Guest")
@Getter
@Setter
public class Guest extends BaseEntity {

    @Column(name = "FullName", length = 100)
    private String fullName;

    @Column(name = "Phone", length = 20)
    private String phone;
}
