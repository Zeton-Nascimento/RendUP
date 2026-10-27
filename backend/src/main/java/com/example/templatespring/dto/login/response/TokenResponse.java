package com.example.templatespring.dto.login.response;

import com.example.templatespring.dto.usuario.response.UsuarioResponse;

public record TokenResponse(
        String accessToken,
        String tipo,
        UsuarioResponse usuario,
        Long expiresIn
) {}

