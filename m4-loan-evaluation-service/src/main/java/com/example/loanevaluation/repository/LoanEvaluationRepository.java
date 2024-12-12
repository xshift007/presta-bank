package com.example.loanevaluation.repository;

import com.example.loanevaluation.entity.LoanEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanEvaluationRepository extends JpaRepository<LoanEvaluation, Long> {

}