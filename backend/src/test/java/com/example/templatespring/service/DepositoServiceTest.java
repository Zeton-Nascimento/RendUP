package com.example.templatespring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.templatespring.dto.deposito.request.DepositoRequest;
import com.example.templatespring.dto.deposito.response.DepositoResponse;
import com.example.templatespring.dto.saldo.response.SaldoResponse;
import com.example.templatespring.entity.MovimentacaoSaldo;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.repository.MovimentacaoSaldoRepository;
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
class DepositoServiceTest {

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MovimentacaoSaldoRepository movimentacaoSaldoRepository;

    @InjectMocks
    private DepositoService depositoService;

    @Test
    void deveDepositarAtualizarSaldoERegistrarMovimentacao() {
        Usuario usuario = usuarioAtivo(new BigDecimal("100.00"));
        when(usuarioAutenticadoService.get()).thenReturn(usuario);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        DepositoRequest request = new DepositoRequest();
        request.setValor(new BigDecimal("50.25"));
        request.setConfirmar(true);

        DepositoResponse response = depositoService.depositar(request);

        assertEquals(new BigDecimal("150.25"), response.getSaldoDisponivel());
        assertEquals(new BigDecimal("150.25"), usuario.getSaldoDisponivel());
        verify(usuarioRepository).save(usuario);
        verify(movimentacaoSaldoRepository).save(any(MovimentacaoSaldo.class));
    }

    @Test
    void deveConsultarSaldoDoUsuarioAutenticado() {
        Usuario usuario = usuarioAtivo(new BigDecimal("123.45"));
        when(usuarioAutenticadoService.get()).thenReturn(usuario);

        SaldoResponse response = depositoService.obterSaldo();

        assertEquals(new BigDecimal("123.45"), response.getSaldoDisponivel());
    }

    @Test
    void deveRejeitarDepositoComValorInvalidoSemAlterarSaldo() {
        Usuario usuario = usuarioAtivo(new BigDecimal("100.00"));

        DepositoRequest request = new DepositoRequest();
        request.setValor(new BigDecimal("10.999"));
        request.setConfirmar(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> depositoService.depositar(request));

        assertEquals(400, exception.getStatusCode().value());
        assertEquals(new BigDecimal("100.00"), usuario.getSaldoDisponivel());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(movimentacaoSaldoRepository, never()).save(any(MovimentacaoSaldo.class));
    }

    @Test
    void deveRejeitarDepositoDeUsuarioInativo() {
        Usuario usuario = usuarioAtivo(new BigDecimal("100.00"));
        usuario.setAtivo(false);
        when(usuarioAutenticadoService.get()).thenThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Usuário não autenticado"));

        DepositoRequest request = new DepositoRequest();
        request.setValor(new BigDecimal("10.00"));
        request.setConfirmar(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> depositoService.depositar(request));

        assertEquals(401, exception.getStatusCode().value());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(movimentacaoSaldoRepository, never()).save(any(MovimentacaoSaldo.class));
    }

    private Usuario usuarioAtivo(BigDecimal saldo) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Investidor");
        usuario.setEmail("investidor@email.com");
        usuario.setSenha("hash");
        usuario.setAtivo(true);
        usuario.setSaldoDisponivel(saldo);
        return usuario;
    }
}
