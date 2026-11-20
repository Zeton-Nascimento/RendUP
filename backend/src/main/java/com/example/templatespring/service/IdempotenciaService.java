package com.example.templatespring.service;

import com.example.templatespring.entity.OperacaoIdempotente;
import com.example.templatespring.entity.enums.StatusOperacaoIdempotencia;
import com.example.templatespring.entity.enums.TipoOperacaoFinanceira;
import com.example.templatespring.exception.IdempotenciaConflitoException;
import com.example.templatespring.repository.OperacaoIdempotenteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class IdempotenciaService {

    private static final int CHAVE_MAXIMA = 100;

    private final OperacaoIdempotenteRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotenciaService(
            OperacaoIdempotenteRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Executa uma operação financeira dentro de uma transação e reaproveita
     * a resposta quando a mesma chave for reenviada com o mesmo comando.
     *
     * O executor deve alterar saldo, investimento e movimentação dentro da
     * mesma transação iniciada por este método. Se ele lançar uma exceção,
     * o registro de idempotência e as alterações financeiras são revertidos.
     */
    @Transactional
    public <T> T executar(
            UUID usuarioId,
            TipoOperacaoFinanceira tipoOperacao,
            String chaveIdempotencia,
            Object comando,
            Class<T> tipoResposta,
            Supplier<T> executor) {
        validarEntrada(usuarioId, tipoOperacao, chaveIdempotencia, executor);

        String chave = chaveIdempotencia.trim();
        String comandoHash = calcularHash(comando);
        OperacaoIdempotente existente = repository
                .findByUsuarioIdAndChaveIdempotenciaForUpdate(usuarioId, chave)
                .orElse(null);

        if (existente != null) {
            validarReenvio(existente, tipoOperacao, comandoHash);
            if (existente.getStatus() == StatusOperacaoIdempotencia.PROCESSANDO) {
                throw new IdempotenciaConflitoException(
                        "A operação com esta chave ainda está sendo processada.");
            }
            return desserializarResposta(existente.getRespostaJson(), tipoResposta);
        }

        OperacaoIdempotente registro = new OperacaoIdempotente();
        registro.setUsuarioId(usuarioId);
        registro.setChaveIdempotencia(chave);
        registro.setTipoOperacao(tipoOperacao);
        registro.setComandoHash(comandoHash);
        registro.setStatus(StatusOperacaoIdempotencia.PROCESSANDO);
        registro.setCreatedAt(LocalDateTime.now());
        repository.saveAndFlush(registro);

        T resposta = executor.get();
        registro.setRespostaJson(serializarResposta(resposta));
        registro.setStatus(StatusOperacaoIdempotencia.CONCLUIDA);
        registro.setCompletedAt(LocalDateTime.now());
        repository.save(registro);
        return resposta;
    }

    private void validarEntrada(
            UUID usuarioId,
            TipoOperacaoFinanceira tipoOperacao,
            String chaveIdempotencia,
            Supplier<?> executor) {
        if (usuarioId == null) {
            throw erro("O usuário da operação é obrigatório.");
        }
        if (tipoOperacao == null) {
            throw erro("O tipo da operação é obrigatório.");
        }
        if (chaveIdempotencia == null || chaveIdempotencia.isBlank()) {
            throw erro("A chave de idempotência é obrigatória.");
        }
        if (chaveIdempotencia.trim().length() > CHAVE_MAXIMA) {
            throw erro("A chave de idempotência deve ter no máximo 100 caracteres.");
        }
        if (executor == null) {
            throw erro("O executor da operação é obrigatório.");
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(BAD_REQUEST, mensagem);
    }

    private void validarReenvio(
            OperacaoIdempotente existente,
            TipoOperacaoFinanceira tipoOperacao,
            String comandoHash) {
        if (existente.getTipoOperacao() != tipoOperacao
                || !existente.getComandoHash().equals(comandoHash)) {
            throw new IdempotenciaConflitoException(
                    "A chave de idempotência já foi usada para outra operação ou comando.");
        }
    }

    private String calcularHash(Object comando) {
        try {
            String json = comando == null ? "null" : objectMapper.writeValueAsString(comando);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(json.getBytes(StandardCharsets.UTF_8)));
        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Não foi possível identificar o comando financeiro.", exception);
        }
    }

    private String serializarResposta(Object resposta) {
        try {
            return objectMapper.writeValueAsString(resposta);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível guardar a resposta da operação.", exception);
        }
    }

    private <T> T desserializarResposta(String respostaJson, Class<T> tipoResposta) {
        try {
            return objectMapper.readValue(respostaJson, tipoResposta);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível recuperar a resposta idempotente.", exception);
        }
    }
}
