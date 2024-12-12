package com.example.loanapplication.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Entidad User que representa un usuario en el sistema.
 */
@Entity
@Table(name = "users") // Renombrar la tabla a 'users'
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private LocalDate birthDate;
    private Double income;

}
