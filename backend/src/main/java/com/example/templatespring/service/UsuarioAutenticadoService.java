package com.example.templatespring.service;

import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getSubject();
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token inválido: email não encontrado");
        }
        return email;
    }

    public Usuario get() {
        return usuarioRepository.findByEmailComCargosEPermissoes(getEmail())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Usuário não autenticado"));
    }
}

