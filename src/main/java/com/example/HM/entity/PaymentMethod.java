package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PaymentMethod")
@Getter
@Setter
@RequiredArgsConstructor
public class PaymentMethod extends BaseEntity {

    @Column(name = "PaymentMethod", length = 50)
    private String methodName;
}
