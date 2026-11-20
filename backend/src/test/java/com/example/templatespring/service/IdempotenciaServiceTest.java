package com.example.templatespring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.templatespring.entity.OperacaoIdempotente;
import com.example.templatespring.entity.enums.StatusOperacaoIdempotencia;
import com.example.templatespring.entity.enums.TipoOperacaoFinanceira;
import com.example.templatespring.exception.IdempotenciaConflitoException;
import com.example.templatespring.repository.OperacaoIdempotenteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdempotenciaServiceTest {

    @Mock
    private OperacaoIdempotenteRepository repository;

    @Test
    void deveExecutarUmaVezEReaproveitarRespostaNoReenvio() {
        IdempotenciaService service = new IdempotenciaService(repository, new ObjectMapper());
        UUID usuarioId = UUID.randomUUID();
        AtomicInteger execucoes = new AtomicInteger();
        when(repository.findByUsuarioIdAndChaveIdempotenciaForUpdate(usuarioId, "chave-1"))
                .thenReturn(Optional.empty(), Optional.of(registroConcluido(usuarioId, "chave-1")));
        when(repository.saveAndFlush(any(OperacaoIdempotente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(any(OperacaoIdempotente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String primeiraResposta = service.executar(
                usuarioId,
                TipoOperacaoFinanceira.DEPOSITO,
                "chave-1",
                comando(100),
                String.class,
                () -> {
                    execucoes.incrementAndGet();
                    return "saldo atualizado";
                });

        String segundaResposta = service.executar(
                usuarioId,
                TipoOperacaoFinanceira.DEPOSITO,
                "chave-1",
                comando(100),
                String.class,
                () -> {
                    execucoes.incrementAndGet();
                    return "não deveria executar novamente";
                });

        assertEquals("saldo atualizado", primeiraResposta);
        assertEquals("saldo atualizado", segundaResposta);
        assertEquals(1, execucoes.get());
    }

    @Test
    void deveRejeitarMesmaChaveComComandoDiferente() {
        IdempotenciaService service = new IdempotenciaService(repository, new ObjectMapper());
        UUID usuarioId = UUID.randomUUID();
        when(repository.findByUsuarioIdAndChaveIdempotenciaForUpdate(usuarioId, "chave-1"))
                .thenReturn(Optional.of(registroConcluido(usuarioId, "chave-1")));

        assertThrows(IdempotenciaConflitoException.class, () -> service.executar(
                usuarioId,
                TipoOperacaoFinanceira.DEPOSITO,
                "chave-1",
                comando(200),
                String.class,
                () -> "não deveria executar"));

        verify(repository, never()).saveAndFlush(any(OperacaoIdempotente.class));
    }

    @Test
    void devePermitirNovaTentativaQuandoExecutorFalhar() {
        IdempotenciaService service = new IdempotenciaService(repository, new ObjectMapper());
        UUID usuarioId = UUID.randomUUID();
        when(repository.findByUsuarioIdAndChaveIdempotenciaForUpdate(usuarioId, "chave-1"))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(OperacaoIdempotente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalStateException.class, () -> service.executar(
                usuarioId,
                TipoOperacaoFinanceira.DEPOSITO,
                "chave-1",
                comando(100),
                String.class,
                () -> {
                    throw new IllegalStateException("falha simulada");
                }));

        verify(repository, never()).save(any(OperacaoIdempotente.class));
    }

    private OperacaoIdempotente registroConcluido(UUID usuarioId, String chave) {
        OperacaoIdempotente registro = new OperacaoIdempotente();
        registro.setUsuarioId(usuarioId);
        registro.setChaveIdempotencia(chave);
        registro.setTipoOperacao(TipoOperacaoFinanceira.DEPOSITO);
        try {
            String json = new ObjectMapper().writeValueAsString(comando(100));
            registro.setComandoHash(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(json.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        registro.setStatus(StatusOperacaoIdempotencia.CONCLUIDA);
        registro.setRespostaJson("\"saldo atualizado\"");
        registro.setCreatedAt(LocalDateTime.now());
        registro.setCompletedAt(LocalDateTime.now());
        return registro;
    }

    private Object comando(int valor) {
        return java.util.Map.of("valor", valor);
    }
}
