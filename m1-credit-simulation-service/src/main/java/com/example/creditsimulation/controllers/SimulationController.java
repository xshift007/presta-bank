package com.example.creditsimulation.controllers;

import com.example.creditsimulation.entity.CreditSimulation;
import com.example.creditsimulation.services.CreditSimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Controlador REST para el microservicio de simulación de crédito.
 */
@RestController
@RequestMapping("/")
@EnableFeignClients // Habilita OpenFeign
public class SimulationController {

    @Autowired
    private CreditSimulationService service;

    /**
     * Endpoint para verificar el estado del servicio.
     * GET /status
     */
    @GetMapping("/status")
    public String status() {
        return "Credit Simulation Service is running.";
    }

    /**
     * Endpoint para simular un crédito.
     * Recibe JSON con: income, requestedAmount, months
     * POST /simulate
     * Ejemplo de petición:
     * {
     *   "income": 5000.0,
     *   "requestedAmount": 20000.0,
     *   "months": 24
     * }
     */
    @PostMapping("/simulate")
    public CreditSimulation simulate(@RequestBody SimulationRequest request) {
        return service.simulate(request.getIncome(), request.getRequestedAmount(), request.getMonths());
    }

    /**
     * Clase interna para mapear la petición JSON a un objeto.
     */
    static class SimulationRequest {
        private Double income;
        private Double requestedAmount;
        private Integer months;

        public Double getIncome() {
            return income;
        }

        public void setIncome(Double income) {
            this.income = income;
        }

        public Double getRequestedAmount() {
            return requestedAmount;
        }

        public void setRequestedAmount(Double requestedAmount) {
            this.requestedAmount = requestedAmount;
        }

        public Integer getMonths() {
            return months;
        }

        public void setMonths(Integer months) {
            this.months = months;
        }
    }
}
