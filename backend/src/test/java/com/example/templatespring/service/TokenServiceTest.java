package com.example.templatespring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import com.example.templatespring.entity.security.Cargo;
import com.example.templatespring.entity.security.Usuario;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncoder;

class TokenServiceTest {

    @Test
    void deveDefinirValidadeDoTokenEmVinteMinutos() {
        TokenService tokenService = new TokenService(mock(JwtEncoder.class));

        assertEquals(1200L, tokenService.getExpiresInSeconds());
    }

    @Test
    void deveIncluirPerfilEAutoridadeNoToken() {
        JwtEncoder encoder = mock(JwtEncoder.class);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("admin@email.com");
        usuario.adicionarCargo(Cargo.builder().nome("ADMIN").build());

        String token = new TokenService(encoder).gerarToken(usuario);

        assertEquals("token", token);
        var captor = org.mockito.ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(encoder).encode(captor.capture());
        var claims = captor.getValue().getClaims();
        assertEquals("ADMIN", claims.getClaim("perfilUsuario"));
        assertTrue(((java.util.Collection<?>) claims.getClaim("authorities")).contains("ROLE_ADMIN"));
    }
}
