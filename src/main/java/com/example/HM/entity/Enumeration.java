package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Enumeration", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"enum_type_id", "enum_code"})
})
@Getter
@Setter
public class Enumeration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enum_type_id", referencedColumnName = "enum_type_id", nullable = false)
    private EnumerationType enumType;

    @Column(name = "enum_code", length = 50, nullable = false)
    private String enumCode;

    @Column(name = "enum_name", length = 100, nullable = false)
    private String enumName;

    @Column(name = "sequence_num")
    private Integer sequenceNum;

    @Column(name = "description", length = 255)
    private String description;
}
