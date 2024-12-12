package com.example.loanapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class M3LoanApplicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(M3LoanApplicationServiceApplication.class, args);
	}
}