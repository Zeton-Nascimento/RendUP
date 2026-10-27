package com.example.templatespring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.repository.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuscarUsuarioServiceTest {

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BuscarUsuarioService buscarUsuarioService;

    @Test
    void investidorDeveConsultarOProprioPerfil() {
        Usuario usuario = criarUsuario("investidor@email.com");
        when(usuarioAutenticadoService.get()).thenReturn(usuario);

        UsuarioResponse response = buscarUsuarioService.buscar();

        assertEquals("investidor@email.com", response.getEmail());
        assertEquals(true, response.isAtivo());
    }

    @Test
    void administradorDeveConsultarUsuarioPorId() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = criarUsuario("consultado@email.com");
        usuario.setId(usuarioId);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = buscarUsuarioService.buscarPorId(usuarioId);

        assertEquals(usuarioId, response.getId());
        assertEquals("consultado@email.com", response.getEmail());
    }

    @Test
    void consultaPorIdDeveRejeitarUsuarioInexistente() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> buscarUsuarioService.buscarPorId(usuarioId));
    }

    private Usuario criarUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário de teste");
        usuario.setEmail(email);
        usuario.setSenha("hash-nao-retornado");
        usuario.setAtivo(true);
        return usuario;
    }
}
