package com.example.creditsimulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
<<<<<<< HEAD
 * Main application class for the Credit Simulation Service.
 * Enables Discovery Client for service registration with Eureka.
=======
 * Clase principal del microservicio de simulación de créditos.
 * Se habilita EurekaClient para registro futuro en Eureka Server.
>>>>>>> origin/main
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // Habilita OpenFeign
public class M1CreditSimulationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(M1CreditSimulationServiceApplication.class, args);
	}

}
