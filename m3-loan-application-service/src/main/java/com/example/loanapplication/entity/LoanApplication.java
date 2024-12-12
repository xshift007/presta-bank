package com.example.loanapplication.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoPrestamo;
    private BigDecimal montoSolicitado;
    private Integer plazoSolicitado;
    private BigDecimal tasaInteres;
    private String estadoSolicitud;
    private String documentosAdjuntos;
    private LocalDateTime fechaSolicitud;
    private String nombreCompleto; // Nuevo campo
}