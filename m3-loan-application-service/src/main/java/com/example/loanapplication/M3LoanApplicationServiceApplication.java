package com.example.loanapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // Habilita OpenFeign
public class M3LoanApplicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(M3LoanApplicationServiceApplication.class, args);
	}
}