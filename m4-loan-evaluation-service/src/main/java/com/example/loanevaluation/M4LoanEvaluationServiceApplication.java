package com.example.loanevaluation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // Habilita OpenFeign
public class M4LoanEvaluationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(M4LoanEvaluationServiceApplication.class, args);
    }
}
