package com.coopmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoopManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoopManagerApplication.class, args);
    }
}
