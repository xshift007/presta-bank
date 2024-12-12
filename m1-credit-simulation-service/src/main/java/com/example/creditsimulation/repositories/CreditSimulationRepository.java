package com.example.creditsimulation.repository;

import com.example.creditsimulation.entity.CreditSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder a los datos de CreditSimulation en la base de datos.
 */
@Repository
public interface CreditSimulationRepository extends JpaRepository<CreditSimulation, Long> {

}
