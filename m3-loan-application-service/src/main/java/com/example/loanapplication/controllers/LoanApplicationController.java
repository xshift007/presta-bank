package com.example.loanapplication.controllers;


import com.example.loanapplication.entity.LoanApplication;
import com.example.loanapplication.service.LoanApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@RestController
@RequestMapping("/")
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService loanApplicationService;

    @GetMapping("/status")
    public String status() {
        return "Loan Application Service is running.";
    }

    @PostMapping(value = "/applications", consumes = "multipart/form-data")
    public ResponseEntity<LoanApplication> createLoanApplication(
            @RequestParam("tipoPrestamo") String tipoPrestamo,
            @RequestParam("montoSolicitado") BigDecimal montoSolicitado,
            @RequestParam("plazoSolicitado") Integer plazoSolicitado,
            @RequestParam("tasaInteres") BigDecimal tasaInteres,
            @RequestParam("comprobanteAvaluo") MultipartFile comprobanteAvaluo,
            @RequestParam("comprobanteIngresos") MultipartFile comprobanteIngresos,
            @RequestParam("nombreCompleto") String nombreCompleto
    ) {
        LoanApplication createdLoan = loanApplicationService.createLoanApplication(
                nombreCompleto, tipoPrestamo, montoSolicitado, plazoSolicitado, tasaInteres, comprobanteAvaluo, comprobanteIngresos);
        return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<LoanApplication> getLoanApplicationById(@PathVariable Long id) {
        LoanApplication loanApplication = loanApplicationService.getLoanApplicationById(id);
        if (loanApplication == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(loanApplication, HttpStatus.OK);
    }
}