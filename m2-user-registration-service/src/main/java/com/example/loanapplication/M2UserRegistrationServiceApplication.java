package com.example.loanapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Clase principal del microservicio de registro de usuarios.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class M2UserRegistrationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(M2UserRegistrationServiceApplication.class, args);
	}
}
