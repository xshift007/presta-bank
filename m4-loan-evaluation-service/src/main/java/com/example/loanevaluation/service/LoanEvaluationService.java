package com.example.loanevaluation.service;

import com.example.loanevaluation.entity.LoanEvaluation;
import com.example.loanevaluation.repository.LoanEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class LoanEvaluationService {

    @Autowired
    private LoanEvaluationRepository loanEvaluationRepository;


    @Autowired
    private RestTemplate restTemplate;


    public LoanEvaluation getEvaluationById(Long id) {
        return loanEvaluationRepository.findById(id).orElse(null);
    }

    public List<LoanEvaluation> getAllEvaluations() {
        return loanEvaluationRepository.findAll();
    }

    public String evaluateLoan(Long idSolicitud) {
        String solicitudServiceUrl = "http://m3-loan-application-service:8080/applications/"+idSolicitud;
        ResponseEntity<LoanApplication> response = restTemplate.getForEntity(solicitudServiceUrl, LoanApplication.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            LoanApplication solicitud = response.getBody();
            LoanEvaluation loanEvaluation = new LoanEvaluation();
            String usuarioServiceUrl = "http://m2-user-registration-service:8080/users/nombre/"+solicitud.getNombreCompleto();
            ResponseEntity<User> responseUser = restTemplate.getForEntity(usuarioServiceUrl, User.class);
            if (responseUser.getStatusCode().is2xxSuccessful()) {
                User usuario = responseUser.getBody();
                // R1: Relación cuota/ingreso <= 40%
                BigDecimal cuotaMensual = calcularCuotaMensual(
                        solicitud.getMontoSolicitado(),
                        solicitud.getPlazoSolicitado(),
                        solicitud.getTasaInteres()
                );
                BigDecimal relacionCuotaIngreso = cuotaMensual.divide(usuario.getIncome(), 2, RoundingMode.HALF_UP);
                loanEvaluation.setRelacionCuotaIngreso(relacionCuotaIngreso);

                if (relacionCuotaIngreso.compareTo(new BigDecimal("0.40")) > 0) {
                    loanEvaluation.setEstadoSolicitud("RECHAZADA");
                    loanEvaluation.setComentariosSeguimiento("Relación cuota/ingreso excede el 40%");
                    loanEvaluation.setFechaAprobacionRechazo(LocalDateTime.now());
                    loanEvaluation.setIdSolicitud(solicitud.getId());
                    loanEvaluation.setNombreCompleto(solicitud.getNombreCompleto());
                    loanEvaluationRepository.save(loanEvaluation);
                    return "RECHAZADA";
                }


                BigDecimal relacionDeudaIngreso = calcularRelacionDeudaIngreso(usuario.getIncome());
                loanEvaluation.setRelacionDeudaIngreso(relacionDeudaIngreso);

                if (relacionDeudaIngreso.compareTo(new BigDecimal("0.50")) > 0) {
                    loanEvaluation.setEstadoSolicitud("RECHAZADA");
                    loanEvaluation.setComentariosSeguimiento("Relación deuda/ingreso excede el 50%");
                    loanEvaluation.setFechaAprobacionRechazo(LocalDateTime.now());
                    loanEvaluation.setIdSolicitud(solicitud.getId());
                    loanEvaluation.setNombreCompleto(solicitud.getNombreCompleto());
                    loanEvaluationRepository.save(loanEvaluation);
                    return "RECHAZADA";
                }

                if (!usuario.getHistorialCrediticio().equalsIgnoreCase("BUENO")) {
                    loanEvaluation.setEstadoSolicitud("RECHAZADA");
                    loanEvaluation.setComentariosSeguimiento("Calificación crediticia insuficiente");
                    loanEvaluation.setFechaAprobacionRechazo(LocalDateTime.now());
                    loanEvaluation.setIdSolicitud(solicitud.getId());
                    loanEvaluation.setNombreCompleto(solicitud.getNombreCompleto());
                    loanEvaluationRepository.save(loanEvaluation);
                    return "RECHAZADA";
                }


                if (solicitud.getPlazoSolicitado() > 30) {
                    loanEvaluation.setEstadoSolicitud("RECHAZADA");
                    loanEvaluation.setComentariosSeguimiento("Plazo solicitado excede los 30 años");
                    loanEvaluation.setFechaAprobacionRechazo(LocalDateTime.now());
                    loanEvaluation.setIdSolicitud(solicitud.getId());
                    loanEvaluation.setNombreCompleto(solicitud.getNombreCompleto());
                    loanEvaluationRepository.save(loanEvaluation);
                    return "RECHAZADA";
                }


                int edadActual = Period.between(usuario.getBirthDate(), LocalDate.now()).getYears();
                int edadAlTermino = edadActual + solicitud.getPlazoSolicitado();
                loanEvaluation.setEdadSolicitanteAlTermino(edadAlTermino);


                if (edadAlTermino > 75) {
                    loanEvaluation.setEstadoSolicitud("RECHAZADA");
                    loanEvaluation.setComentariosSeguimiento("Edad al término del préstamo excede los 75 años");
                    loanEvaluation.setFechaAprobacionRechazo(LocalDateTime.now());
                    loanEvaluation.setIdSolicitud(solicitud.getId());
                    loanEvaluation.setNombreCompleto(solicitud.getNombreCompleto());
                    loanEvaluationRepository.save(loanEvaluation);
                    return "RECHAZADA";
                }


                if (usuario.getAntiguedadLaboral() < 2) {
                    loanEvaluation.setEstadoSolicitud("RECHAZADA");
                    loanEvaluation.setComentariosSeguimiento("Antigüedad laboral insuficiente");
                    loanEvaluation.setFechaAprobacionRechazo(LocalDateTime.now());
                    loanEvaluation.setIdSolicitud(solicitud.getId());
                    loanEvaluation.setNombreCompleto(solicitud.getNombreCompleto());
                    loanEvaluationRepository.save(loanEvaluation);
                    return "RECHAZADA";
                }


                if (usuario.getCapacidadAhorro() == null || !usuario.getCapacidadAhorro().equalsIgnoreCase("ADECUADA")) {
                    loanEvaluation.setEstadoSolicitud("RECHAZADA");
                    loanEvaluation.setComentariosSeguimiento("Capacidad de ahorro insuficiente");
                    loanEvaluation.setFechaAprobacionRechazo(LocalDateTime.now());
                    loanEvaluation.setIdSolicitud(solicitud.getId());
                    loanEvaluation.setNombreCompleto(solicitud.getNombreCompleto());
                    loanEvaluationRepository.save(loanEvaluation);
                    return "RECHAZADA";
                }



                loanEvaluation.setEstadoSolicitud("APROBADA");
                loanEvaluation.setFechaAprobacionRechazo(LocalDateTime.now());
                loanEvaluation.setComentariosSeguimiento("Solicitud aprobada");
                loanEvaluation.setIdSolicitud(solicitud.getId());
                loanEvaluation.setNombreCompleto(solicitud.getNombreCompleto());
                loanEvaluationRepository.save(loanEvaluation);
                return "APROBADA";
            } else {
                return "Usuario no encontrado en el servicio de registro de usuario";
            }
        } else {
            return "Solicitud no encontrada en el servicio de solicitud";
        }
    }
    private BigDecimal calcularCuotaMensual(BigDecimal monto, Integer plazoAnios, BigDecimal tasaAnual) {
        Integer plazoMeses = plazoAnios * 12;
        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal unoMasR = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = unoMasR.pow(plazoMeses, MathContext.DECIMAL128);
        BigDecimal cuotaMensual = monto.multiply(tasaMensual.multiply(potencia))
                .divide(potencia.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        return cuotaMensual;
    }

    private BigDecimal calcularRelacionDeudaIngreso(BigDecimal ingresos) {
        return  BigDecimal.ZERO.divide(ingresos, 2, RoundingMode.HALF_UP);
    }


    static class LoanApplication {

        private Long id;

        private String tipoPrestamo;
        private BigDecimal montoSolicitado;
        private Integer plazoSolicitado;
        private BigDecimal tasaInteres;

        private String estadoSolicitud;
        private String documentosAdjuntos;
        private LocalDateTime fechaSolicitud;
        private String nombreCompleto;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTipoPrestamo() {
            return tipoPrestamo;
        }

        public void setTipoPrestamo(String tipoPrestamo) {
            this.tipoPrestamo = tipoPrestamo;
        }

        public BigDecimal getMontoSolicitado() {
            return montoSolicitado;
        }

        public void setMontoSolicitado(BigDecimal montoSolicitado) {
            this.montoSolicitado = montoSolicitado;
        }

        public Integer getPlazoSolicitado() {
            return plazoSolicitado;
        }

        public void setPlazoSolicitado(Integer plazoSolicitado) {
            this.plazoSolicitado = plazoSolicitado;
        }

        public BigDecimal getTasaInteres() {
            return tasaInteres;
        }

        public void setTasaInteres(BigDecimal tasaInteres) {
            this.tasaInteres = tasaInteres;
        }

        public String getEstadoSolicitud() {
            return estadoSolicitud;
        }

        public void setEstadoSolicitud(String estadoSolicitud) {
            this.estadoSolicitud = estadoSolicitud;
        }

        public String getDocumentosAdjuntos() {
            return documentosAdjuntos;
        }

        public void setDocumentosAdjuntos(String documentosAdjuntos) {
            this.documentosAdjuntos = documentosAdjuntos;
        }

        public LocalDateTime getFechaSolicitud() {
            return fechaSolicitud;
        }

        public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
            this.fechaSolicitud = fechaSolicitud;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public void setNombreCompleto(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
        }
    }

    static class User {
        private Long id;
        private String fullName;
        private String email;
        private LocalDate birthDate;
        private BigDecimal income;
        private String historialCrediticio;
        private Integer antiguedadLaboral;
        private String capacidadAhorro;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public LocalDate getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
        }

        public BigDecimal getIncome() {
            return income;
        }

        public void setIncome(BigDecimal income) {
            this.income = income;
        }

        public String getHistorialCrediticio() {
            return historialCrediticio;
        }

        public void setHistorialCrediticio(String historialCrediticio) {
            this.historialCrediticio = historialCrediticio;
        }

        public Integer getAntiguedadLaboral() {
            return antiguedadLaboral;
        }

        public void setAntiguedadLaboral(Integer antiguedadLaboral) {
            this.antiguedadLaboral = antiguedadLaboral;
        }

        public String getCapacidadAhorro() {
            return capacidadAhorro;
        }

        public void setCapacidadAhorro(String capacidadAhorro) {
            this.capacidadAhorro = capacidadAhorro;
        }
    }


}