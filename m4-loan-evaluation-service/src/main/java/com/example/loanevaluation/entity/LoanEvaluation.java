package com.example.loanevaluation.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class LoanEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idSolicitud;
    private String nombreCompleto;
    private BigDecimal relacionCuotaIngreso;
    private BigDecimal relacionDeudaIngreso;
    private String resultadoEvaluacionHistorialCrediticio;
    private BigDecimal montoFinanciamientoAprobado;
    private BigDecimal porcentajeFinanciamiento;
    private Integer edadSolicitanteAlTermino;
    private String resultadoEvaluacionAntiguedadLaboral;
    private LocalDateTime fechaAprobacionRechazo;
    private LocalDateTime fechaDesembolso;
    private String comentariosSeguimiento;
    private String estadoSolicitud;

}