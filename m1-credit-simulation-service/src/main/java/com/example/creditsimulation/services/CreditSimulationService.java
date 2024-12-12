package com.example.creditsimulation.services;

import com.example.creditsimulation.entity.CreditSimulation;
import com.example.creditsimulation.repositories.CreditSimulationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Servicio que contiene la lógica para simular el crédito.
 */
@EnableFeignClients // Habilita OpenFeign
@Service
public class CreditSimulationService {

    @Autowired
    private CreditSimulationRepository repository;

    // Tasa de interés fija del 10%
    private final double interestRate = 0.1;

    /**
     * Realiza el cálculo de la simulación y la guarda en DB.
     *
     * @param income Ingreso mensual del usuario
     * @param requestedAmount Monto solicitado
     * @param months Plazo en meses
     * @return la entidad CreditSimulation con cálculos realizados
     */
    public CreditSimulation simulate(Double income, Double requestedAmount, Integer months) {
        // Calcular totalPayment
        double totalPayment = requestedAmount * (1 + interestRate);
        // Calcular monthlyPayment
        double monthlyPayment = totalPayment / months;

        CreditSimulation simulation = new CreditSimulation();
        simulation.setIncome(income);
        simulation.setRequestedAmount(requestedAmount);
        simulation.setMonths(months);
        simulation.setTotalPayment(totalPayment);
        simulation.setMonthlyPayment(monthlyPayment);

        // Guardar la simulación en la DB
        return repository.save(simulation);
    }

    /**
     * Método opcional para obtener una simulación por su ID.
     */
    public CreditSimulation getSimulationById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
