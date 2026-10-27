package com.example.templatespring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class InativarUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private InativarUsuarioService service;

    @Test
    void deveInativarSemExcluirNemAlterarDadosFinanceiros() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        Usuario administrador = new Usuario();
        administrador.setId(UUID.randomUUID());
        usuario.setAtivo(true);
        usuario.setNome("Maria Oliveira");
        usuario.setEmail("maria@email.com");
        usuario.setSaldoDisponivel(BigDecimal.valueOf(250));
        when(usuarioAutenticadoService.get()).thenReturn(administrador);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        var response = service.inativar(usuarioId, true);

        assertEquals(false, usuario.isAtivo());
        assertEquals(0, BigDecimal.valueOf(250).compareTo(usuario.getSaldoDisponivel()));
        assertEquals(false, response.isAtivo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveRejeitarUsuarioInexistente() {
        UUID usuarioId = UUID.randomUUID();
        Usuario administrador = new Usuario();
        administrador.setId(UUID.randomUUID());
        when(usuarioAutenticadoService.get()).thenReturn(administrador);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.inativar(usuarioId, true));
    }

    @Test
    void deveManterEstadoQuandoUsuarioJaEstaInativo() {
        UUID usuarioId = UUID.randomUUID();
        Usuario administrador = new Usuario();
        administrador.setId(UUID.randomUUID());
        when(usuarioAutenticadoService.get()).thenReturn(administrador);
        Usuario usuario = new Usuario();
        usuario.setAtivo(false);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.inativar(usuarioId, true));

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void administradorNaoDeveInativarAPropriaConta() {
        UUID administradorId = UUID.randomUUID();
        Usuario administrador = new Usuario();
        administrador.setId(administradorId);
        when(usuarioAutenticadoService.get()).thenReturn(administrador);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.inativar(administradorId, true));

        assertEquals(409, exception.getStatusCode().value());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveExigirConfirmacaoAntesDeConsultarUsuario() {
        UUID usuarioId = UUID.randomUUID();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.inativar(usuarioId, false));

        assertEquals(400, exception.getStatusCode().value());
        verifyNoInteractions(usuarioRepository);
    }
}
