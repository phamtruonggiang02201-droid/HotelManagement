package com.example.HM.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VNPayConfig {

    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String version;
    private String command;
    private String orderType;
}
