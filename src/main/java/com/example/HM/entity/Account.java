package com.example.HM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Accounts")
@Getter
@Setter
public class Account extends BaseEntity {

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "status", nullable = false)
    private Boolean status = true; // 1 = active, 0 = locked

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "phone", length = 20, unique = true)
    private String phone;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "id_number", length = 50)
    private String idNumber; // CCCD, CMND hoặc Passport

    @Column(name = "id_type", length = 20)
    private String idType; // CCCD, CMND, PASSPORT

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "reset_token", length = 100)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "verification_token", length = 100)
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;
}
