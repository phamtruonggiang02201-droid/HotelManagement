package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "Guest")
@Getter
@Setter
public class Guest extends BaseEntity {

    @Column(name = "FullName", length = 100)
    private String fullName;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "id_number", length = 50)
    private String idNumber;

    @Column(name = "id_type", length = 20)
    private String idType;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "Email", length = 100)
    private String email;
}
