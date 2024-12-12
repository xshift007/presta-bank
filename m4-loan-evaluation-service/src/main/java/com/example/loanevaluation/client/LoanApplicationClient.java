package com.example.loanevaluation.client;

import com.example.loanevaluation.service.LoanEvaluationService.LoanApplication;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign para comunicarse con el servicio de Loan Application.
 */
@FeignClient(name = "m3-loan-application-service")
public interface LoanApplicationClient {

    @GetMapping("/applications/{id}")
    LoanApplication getLoanApplicationById(@PathVariable("id") Long id);
}
