package com.airbnbspa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirBnbSpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirBnbSpaApplication.class, args);
    }
}