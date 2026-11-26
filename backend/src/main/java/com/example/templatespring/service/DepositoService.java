package com.example.templatespring.service;

import com.example.templatespring.dto.deposito.request.DepositoRequest;
import com.example.templatespring.dto.deposito.response.DepositoResponse;
import com.example.templatespring.dto.saldo.response.SaldoResponse;
import com.example.templatespring.entity.MovimentacaoSaldo;
import com.example.templatespring.entity.enums.TipoMovimentacaoSaldo;
import com.example.templatespring.entity.enums.TipoOperacaoFinanceira;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.repository.MovimentacaoSaldoRepository;
import com.example.templatespring.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class DepositoService {

    private static final BigDecimal ZERO_DINHEIRO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoSaldoRepository movimentacaoSaldoRepository;
    private final IdempotenciaService idempotenciaService;

    public DepositoService(
            UsuarioAutenticadoService usuarioAutenticadoService,
            UsuarioRepository usuarioRepository,
            MovimentacaoSaldoRepository movimentacaoSaldoRepository,
            IdempotenciaService idempotenciaService) {
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioRepository = usuarioRepository;
        this.movimentacaoSaldoRepository = movimentacaoSaldoRepository;
        this.idempotenciaService = idempotenciaService;
    }

    @Transactional
    public DepositoResponse depositar(DepositoRequest request) {
        return depositar(request, null);
    }

    @Transactional
    public DepositoResponse depositar(DepositoRequest request, String chaveIdempotencia) {
        validarValor(request);

        Usuario autenticado = usuarioAutenticadoService.get();
        if (chaveIdempotencia == null || chaveIdempotencia.isBlank()) {
            return executarDeposito(autenticado.getId(), request);
        }

        return idempotenciaService.executar(
                autenticado.getId(),
                TipoOperacaoFinanceira.DEPOSITO,
                chaveIdempotencia,
                request,
                DepositoResponse.class,
                () -> executarDeposito(autenticado.getId(), request));
    }

    private DepositoResponse executarDeposito(UUID usuarioId, DepositoRequest request) {
        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Usuário não autenticado"));

        if (!usuario.isAtivo()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Usuário não autenticado");
        }

        BigDecimal valor = normalizarValor(request.getValor());
        BigDecimal saldoAntes = normalizarSaldo(usuario.getSaldoDisponivel());
        BigDecimal saldoApos = saldoAntes.add(valor).setScale(2, RoundingMode.HALF_UP);

        usuario.setSaldoDisponivel(saldoApos);
        usuarioRepository.save(usuario);
        registrarMovimentacao(usuario, valor, saldoAntes, saldoApos);

        return DepositoResponse.builder()
                .saldoDisponivel(saldoApos)
                .build();
    }

    @Transactional(readOnly = true)
    public SaldoResponse obterSaldo() {
        Usuario usuario = usuarioAutenticadoService.get();
        BigDecimal saldo = normalizarSaldo(usuario.getSaldoDisponivel());
        return SaldoResponse.builder()
                .saldoDisponivel(saldo)
                .build();
    }

    private void validarValor(DepositoRequest request) {
        if (request == null || !request.isConfirmar()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "A confirmação do depósito é obrigatória.");
        }

        if (request == null || request.getValor() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Valor do depósito é obrigatório.");
        }

        if (request.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Valor do depósito deve ser maior que zero.");
        }

        if (request.getValor().scale() > 2) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Valor do depósito deve possuir no máximo duas casas decimais.");
        }
    }

    private BigDecimal normalizarValor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizarSaldo(BigDecimal saldo) {
        return saldo == null ? ZERO_DINHEIRO : saldo.setScale(2, RoundingMode.HALF_UP);
    }

    private void registrarMovimentacao(
            Usuario usuario,
            BigDecimal valor,
            BigDecimal saldoAntes,
            BigDecimal saldoApos) {
        MovimentacaoSaldo movimentacao = new MovimentacaoSaldo();
        movimentacao.setUsuario(usuario);
        movimentacao.setTipo(TipoMovimentacaoSaldo.DEPOSITO);
        movimentacao.setValor(valor);
        movimentacao.setSaldoAntes(saldoAntes);
        movimentacao.setSaldoApos(saldoApos);
        movimentacao.setDataMovimentacao(LocalDateTime.now());
        movimentacaoSaldoRepository.save(movimentacao);
    }
}
