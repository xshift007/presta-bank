package com.example.loanevaluation.client;

import com.example.loanevaluation.service.LoanEvaluationService.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign para comunicarse con el servicio de User Registration.
 */
@FeignClient(name = "m2-user-registration-service")
public interface UserRegistrationClient {

    @GetMapping("/users/nombre/{nombreCompleto}")
    User getUserByNombreCompleto(@PathVariable("nombreCompleto") String nombreCompleto);
}
