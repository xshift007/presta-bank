package com.example.creditsimulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
/**
 * Clase principal del microservicio de simulación de créditos.
 * Main application class for the Credit Simulation Service.
 * Se habilita EurekaClient para registro futuro en Eureka Server.
 * Enables Discovery Client for service registration with Eureka.
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // Habilita OpenFeign
public class M1CreditSimulationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(M1CreditSimulationServiceApplication.class, args);
	}

}
