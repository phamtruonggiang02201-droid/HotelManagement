package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ServiceCategories")
@Getter
@Setter
@RequiredArgsConstructor
public class ServiceCategory extends BaseEntity {

    @Column(name = "CategoryName", length = 100)
    private String categoryName;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "description")
    private String description;

    @Column(name = "required_role", length = 50)
    private String requiredRole; // VD: ROLE_CHEF, ROLE_MASSAGE...
}
