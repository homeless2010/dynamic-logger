package com.dlog.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.dlog")
public class DynamicLoggerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DynamicLoggerApplication.class, args);
    }
}
