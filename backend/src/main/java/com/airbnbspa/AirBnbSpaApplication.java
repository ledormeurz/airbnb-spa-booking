package com.airbnbspa;

import com.airbnbspa.config.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(RateLimitProperties.class)
public class AirBnbSpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirBnbSpaApplication.class, args);
    }
}