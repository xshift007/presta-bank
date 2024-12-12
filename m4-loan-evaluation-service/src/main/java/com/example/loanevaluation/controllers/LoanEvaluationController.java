package com.example.loanevaluation.controller;

import com.example.loanevaluation.entity.LoanEvaluation;
import com.example.loanevaluation.service.LoanEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class LoanEvaluationController {

    @Autowired
    private LoanEvaluationService loanEvaluationService;

    @GetMapping("/status")
    public String status() {
        return "Loan Evaluation Service is running.";
    }


    @GetMapping("/evaluations")
    public ResponseEntity<List<LoanEvaluation>> getAllEvaluations() {
        List<LoanEvaluation> evaluations = loanEvaluationService.getAllEvaluations();
        return new ResponseEntity<>(evaluations, HttpStatus.OK);
    }


    @GetMapping("/evaluations/{id}")
    public ResponseEntity<LoanEvaluation> getEvaluationById(@PathVariable Long id) {
        LoanEvaluation loanEvaluation= loanEvaluationService.getEvaluationById(id);
        if (loanEvaluation == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(loanEvaluation, HttpStatus.OK);
    }

    @PutMapping("/evaluations/{id}/evaluate")
    public ResponseEntity<String> evaluateLoan(@PathVariable Long id) {
        try {
            String result = loanEvaluationService.evaluateLoan(id);
            return new ResponseEntity<>(result,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}