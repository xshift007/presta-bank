package com.example.creditsimulation.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa una simulación de crédito.
 * Se almacenará en la base de datos.
 */
@Entity
@Data
public class CreditSimulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double income;          // Ingreso mensual del usuario
    private Double requestedAmount; // Monto solicitado
    private Integer months;         // Plazo en meses

    private Double totalPayment;    // Monto total a pagar (calculado)
    private Double monthlyPayment;  // Cuota mensual (calculado)
}
