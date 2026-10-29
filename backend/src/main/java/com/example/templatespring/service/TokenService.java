package com.example.templatespring.service;

import com.example.templatespring.entity.security.Cargo;
import com.example.templatespring.entity.security.Permissao;
import com.example.templatespring.entity.security.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private long expiresInSeconds = 1200L;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Value("${jwt.expiration-seconds:1200}")
    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public String gerarToken(Usuario usuario) {
        Set<String> authorities = new LinkedHashSet<>();
        for (Cargo cargo : usuario.getCargos()) {
            authorities.add("ROLE_" + cargo.getNome());
            for (Permissao permissao : cargo.getPermissoes()) {
                authorities.add(permissao.getNome());
            }
        }

        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("investimentos-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresInSeconds))
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("authorities", authorities);

        if (usuario.getPerfilUsuario() != null) {
            claimsBuilder.claim("perfilUsuario", usuario.getPerfilUsuario());
        }

        JwtClaimsSet claims = claimsBuilder.build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
