package com.example.templatespring.entity.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class UsuarioPerfilTest {

    @Test
    void deveIdentificarPerfilAdministrador() {
        Usuario usuario = new Usuario();
        usuario.adicionarCargo(Cargo.builder().nome("ADMIN").build());

        assertEquals("ADMIN", usuario.getPerfilUsuario());
    }

    @Test
    void deveIdentificarPerfilInvestidor() {
        Usuario usuario = new Usuario();
        usuario.adicionarCargo(Cargo.builder().nome("INVESTIDOR").build());

        assertEquals("INVESTIDOR", usuario.getPerfilUsuario());
    }

    @Test
    void deveRetornarPerfilNuloQuandoCargoNaoForReconhecido() {
        Usuario usuario = new Usuario();
        usuario.adicionarCargo(Cargo.builder().nome("OUTRO").build());

        assertNull(usuario.getPerfilUsuario());
    }
}
