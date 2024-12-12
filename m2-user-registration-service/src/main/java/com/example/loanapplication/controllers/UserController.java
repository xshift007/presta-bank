package com.example.loanapplication.controller;

import com.example.loanapplication.entity.User;
import com.example.loanapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el manejo de usuarios.
 */
@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Endpoint para verificar el estado del servicio.
     */
    @GetMapping("/status")
    public String status() {
        return "User Registration Service is running.";
    }

    /**
     * POST /users
     * Ejemplo de JSON para registrar usuario:
     * {
     *   "fullName": "John Doe",
     *   "email": "john.doe@example.com",
     *   "birthDate": "1990-05-20",
     *   "income": 4500.0
     * }
     */
    @PostMapping("/users")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    /**
     * GET /users/{id}
     * Obtener un usuario por su ID
     */
    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
