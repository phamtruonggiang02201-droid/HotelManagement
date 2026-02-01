package com.example.HM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PaymentMethod")
@Getter
@Setter
public class PaymentMethod extends BaseEntity {

    @Column(name = "PaymentMethod", length = 50)
    private String methodName;
}
