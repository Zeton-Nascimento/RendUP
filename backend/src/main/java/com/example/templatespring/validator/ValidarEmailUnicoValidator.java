package com.example.templatespring.validator;

import com.example.templatespring.repository.UsuarioRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Component
public class ValidarEmailUnicoValidator {

    private final UsuarioRepository repository;

    public ValidarEmailUnicoValidator(UsuarioRepository repository) {
        this.repository = repository;
    }

    public void validar(String email) {
        if (email == null || repository.existsByEmailIgnoreCase(email.trim())) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "Este e-mail já está sendo usado.");
        }
    }
}
