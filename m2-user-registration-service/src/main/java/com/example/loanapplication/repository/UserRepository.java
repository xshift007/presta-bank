package com.example.loanapplication.repository;

import com.example.loanapplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Se pueden agregar métodos de búsqueda personalizados, si es necesario
}
