package com.example.templatespring.service;

import com.example.templatespring.entity.security.Cargo;
import com.example.templatespring.entity.security.Permissao;
import com.example.templatespring.entity.security.Usuario;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class TokenService {

    private static final long EXPIRES_IN_SECONDS = 86400L;

    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
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
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("investimentos-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS))
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("authorities", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long getExpiresInSeconds() {
        return EXPIRES_IN_SECONDS;
    }
}