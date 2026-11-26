package com.example.templatespring.service;

import com.example.templatespring.dto.resgate.request.PreviewResgateRequest;
import com.example.templatespring.dto.resgate.request.ResgateRequest;
import com.example.templatespring.dto.resgate.response.PreviewResgateResponse;
import com.example.templatespring.dto.resgate.response.ResgateInvestimentoResponse;
import com.example.templatespring.dto.resgate.response.ResgateResponse;
import com.example.templatespring.entity.Investimento;
import com.example.templatespring.entity.MovimentacaoSaldo;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.entity.enums.StatusInvestimento;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoMovimentacaoSaldo;
import com.example.templatespring.entity.enums.TipoOperacaoFinanceira;
import com.example.templatespring.entity.enums.TipoProduto;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.repository.InvestimentoRepository;
import com.example.templatespring.repository.MovimentacaoSaldoRepository;
import com.example.templatespring.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ResgateService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal QUINZE_PORCENTO = new BigDecimal("0.15");
    private static final BigDecimal VINTE_PORCENTO = new BigDecimal("0.20");
    private static final BigDecimal VINTE_E_DOIS_E_MEIO_PORCENTO = new BigDecimal("0.225");
    private static final BigDecimal DEZESSETE_E_MEIO_PORCENTO = new BigDecimal("0.175");

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioRepository usuarioRepository;
    private final InvestimentoRepository investimentoRepository;
    private final MovimentacaoSaldoRepository movimentacaoSaldoRepository;
    private final IdempotenciaService idempotenciaService;

    public ResgateService(
            UsuarioAutenticadoService usuarioAutenticadoService,
            UsuarioRepository usuarioRepository,
            InvestimentoRepository investimentoRepository,
            MovimentacaoSaldoRepository movimentacaoSaldoRepository,
            IdempotenciaService idempotenciaService) {
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioRepository = usuarioRepository;
        this.investimentoRepository = investimentoRepository;
        this.movimentacaoSaldoRepository = movimentacaoSaldoRepository;
        this.idempotenciaService = idempotenciaService;
    }

    @Transactional(readOnly = true)
    public ResgateInvestimentoResponse consultar(UUID investimentoId) {
        Usuario usuario = usuarioAutenticadoService.get();
        Investimento investimento = buscarDoUsuario(investimentoId, usuario);
        BigDecimal valorAtual = dinheiro(investimento.getValorAtual());
        return ResgateInvestimentoResponse.builder()
                .investimentoId(investimento.getId())
                .nomeProduto(investimento.getProdutoFinanceiro().getNome())
                .tipoProduto(investimento.getProdutoFinanceiro().getTipoProduto())
                .status(investimento.getStatus())
                .valorAplicado(dinheiro(investimento.getValorAplicado()))
                .valorAtual(valorAtual)
                .valorResgatavel(investimento.isBloqueado() ? ZERO : valorAtual)
                .dataAplicacao(investimento.getDataAplicacao())
                .dataVencimento(investimento.getDataVencimento())
                .carenciaDias(investimento.getCarenciaDiasContratada())
                .liquidez(investimento.getProdutoFinanceiro().getLiquidez())
                .nivelRisco(investimento.getProdutoFinanceiro().getNivelRisco())
                .bloqueado(investimento.isBloqueado())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ResgateInvestimentoResponse> listarDisponiveis() {
        Usuario usuario = usuarioAutenticadoService.get();
        return investimentoRepository
                .findByUsuarioIdAndStatusOrderByCreatedAtDesc(usuario.getId(), StatusInvestimento.ATIVO)
                .stream()
                .map(investimento -> {
                    BigDecimal valorAtual = dinheiro(investimento.getValorAtual());
                    return ResgateInvestimentoResponse.builder()
                            .investimentoId(investimento.getId())
                            .nomeProduto(investimento.getProdutoFinanceiro().getNome())
                            .tipoProduto(investimento.getProdutoFinanceiro().getTipoProduto())
                            .status(investimento.getStatus())
                            .valorAplicado(dinheiro(investimento.getValorAplicado()))
                            .valorAtual(valorAtual)
                            .valorResgatavel(investimento.isBloqueado() ? ZERO : valorAtual)
                            .dataAplicacao(investimento.getDataAplicacao())
                            .dataVencimento(investimento.getDataVencimento())
                            .carenciaDias(investimento.getCarenciaDiasContratada())
                            .liquidez(investimento.getProdutoFinanceiro().getLiquidez())
                            .nivelRisco(investimento.getProdutoFinanceiro().getNivelRisco())
                            .bloqueado(investimento.isBloqueado())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PreviewResgateResponse visualizarPrevia(UUID investimentoId, PreviewResgateRequest request) {
        Usuario usuario = usuarioAutenticadoService.get();
        Investimento investimento = buscarDoUsuario(investimentoId, usuario);
        return montarPrevia(investimento, request == null ? null : request.getValorResgate());
    }

    public ResgateResponse resgatar(UUID investimentoId, ResgateRequest request, String chaveIdempotencia) {
        Usuario usuario = usuarioAutenticadoService.get();
        return idempotenciaService.executar(
                usuario.getId(),
                TipoOperacaoFinanceira.RESGATE,
                chaveIdempotencia,
                new ComandoResgate(investimentoId, request),
                ResgateResponse.class,
                () -> executarResgate(usuario.getId(), investimentoId, request));
    }

    @Transactional
    protected ResgateResponse executarResgate(UUID usuarioId, UUID investimentoId, ResgateRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getConfirmar())) {
            throw new ResponseStatusException(BAD_REQUEST, "A confirmação do resgate é obrigatória.");
        }

        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Usuário não encontrado."));
        Investimento investimento = investimentoRepository.findByIdForUpdate(investimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Investimento não encontrado."));

        validarProprietario(investimento, usuario);
        BigDecimal valorSolicitado = request.getValorResgate();
        List<String> erros = validar(investimento, valorSolicitado);
        if (!erros.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, String.join(" ", erros));
        }

        Calculo calculo = calcular(investimento, valorSolicitado);
        BigDecimal saldoAntes = dinheiro(usuario.getSaldoDisponivel());
        BigDecimal saldoApos = saldoAntes.add(calculo.valorLiquido()).setScale(2, RoundingMode.HALF_UP);
        LocalDateTime agora = LocalDateTime.now();

        if (calculo.total()) {
            investimento.setStatus(StatusInvestimento.RESGATADO);
            investimento.setValorAtual(ZERO);
        } else {
            investimento.setValorAtual(calculo.saldoRemanescente());
            investimento.setValorAplicado(novoValorAplicado(
                    dinheiro(investimento.getValorAplicado()),
                    calculo.saldoRemanescente(),
                    calculo.valorAtual()));
        }
        investimento.setUpdatedAt(agora);
        Investimento salvo = investimentoRepository.save(investimento);

        usuario.setSaldoDisponivel(saldoApos);
        usuarioRepository.save(usuario);

        MovimentacaoSaldo movimentacao = new MovimentacaoSaldo();
        movimentacao.setUsuario(usuario);
        movimentacao.setInvestimento(salvo);
        movimentacao.setTipo(TipoMovimentacaoSaldo.RESGATE);
        movimentacao.setValor(calculo.valorLiquido());
        movimentacao.setSaldoAntes(saldoAntes);
        movimentacao.setSaldoApos(saldoApos);
        movimentacao.setDataMovimentacao(agora);
        movimentacaoSaldoRepository.save(movimentacao);

        return ResgateResponse.builder()
                .investimentoId(investimentoId)
                .valorBruto(calculo.valorBruto())
                .imposto(calculo.imposto())
                .descontoAntecipacao(calculo.descontoAntecipacao())
                .valorLiquido(calculo.valorLiquido())
                .saldoRemanescente(calculo.saldoRemanescente())
                .saldoDisponivel(saldoApos)
                .antecipado(calculo.antecipado())
                .resgatadoEm(agora)
                .build();
    }

    private PreviewResgateResponse montarPrevia(Investimento investimento, BigDecimal valorSolicitado) {
        List<String> erros = validar(investimento, valorSolicitado);
        Calculo calculo = podeCalcular(investimento, valorSolicitado)
                ? calcular(investimento, valorSolicitado)
                : null;
        BigDecimal valorAtual = dinheiro(investimento.getValorAtual());

        return PreviewResgateResponse.builder()
                .validoParaConfirmacao(erros.isEmpty())
                .erros(erros)
                .investimentoId(investimento.getId())
                .nomeProduto(investimento.getProdutoFinanceiro().getNome())
                .status(investimento.getStatus())
                .valorAtual(valorAtual)
                .valorResgatavel(investimento.isBloqueado() ? ZERO : valorAtual)
                .valorBruto(calculo == null ? null : calculo.valorBruto())
                .impostoEstimado(calculo == null ? null : calculo.imposto())
                .descontoAntecipacao(calculo == null ? null : calculo.descontoAntecipacao())
                .valorLiquido(calculo == null ? null : calculo.valorLiquido())
                .saldoRemanescente(calculo == null ? null : calculo.saldoRemanescente())
                .antecipado(calculo != null && calculo.antecipado())
                .dataAplicacao(investimento.getDataAplicacao())
                .dataVencimento(investimento.getDataVencimento())
                .carenciaDias(investimento.getCarenciaDiasContratada())
                .liquidez(investimento.getProdutoFinanceiro().getLiquidez())
                .nivelRisco(investimento.getProdutoFinanceiro().getNivelRisco())
                .bloqueado(investimento.isBloqueado())
                .build();
    }

    private List<String> validar(Investimento investimento, BigDecimal valorSolicitado) {
        List<String> erros = new ArrayList<>();
        BigDecimal valorAtual = dinheiro(investimento.getValorAtual());

        if (investimento.getStatus() != StatusInvestimento.ATIVO) {
            erros.add("O investimento não está ativo para resgate.");
        }
        if (investimento.isBloqueado()) {
            erros.add("O investimento possui bloqueio judicial ativo e não pode ser resgatado.");
        }
        if (valorAtual.compareTo(BigDecimal.ZERO) <= 0) {
            erros.add("O investimento não possui saldo aplicado para resgate.");
        }
        if (valorSolicitado != null) {
            if (valorSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
                erros.add("Informe um valor de resgate maior que zero.");
            } else if (valorSolicitado.scale() > 2) {
                erros.add("O valor do resgate deve possuir no máximo duas casas decimais.");
            } else if (valorSolicitado.compareTo(valorAtual) > 0) {
                erros.add("O valor solicitado é maior que o valor resgatável.");
            }
        }

        Integer carencia = investimento.getCarenciaDiasContratada();
        if (carencia != null && carencia > 0 && investimento.getDataAplicacao() != null
                && LocalDate.now().isBefore(investimento.getDataAplicacao().plusDays(carencia))) {
            erros.add("A carência ainda está vigente. Aguarde o prazo contratado.");
        }

        ProdutoFinanceiro produto = investimento.getProdutoFinanceiro();
        if (produto != null && produto.getLiquidez() == TipoLiquidez.NO_VENCIMENTO
                && investimento.getDataVencimento() != null
                && LocalDate.now().isBefore(investimento.getDataVencimento())) {
            erros.add("O resgate deste produto é permitido somente no vencimento.");
        }
        return erros;
    }

    private boolean podeCalcular(Investimento investimento, BigDecimal valorSolicitado) {
        BigDecimal atual = dinheiro(investimento.getValorAtual());
        return investimento.getStatus() == StatusInvestimento.ATIVO
                && !investimento.isBloqueado()
                && atual.compareTo(BigDecimal.ZERO) > 0
                && (valorSolicitado == null
                || (valorSolicitado.compareTo(BigDecimal.ZERO) > 0
                && valorSolicitado.scale() <= 2
                && valorSolicitado.compareTo(atual) <= 0));
    }

    private Calculo calcular(Investimento investimento, BigDecimal valorSolicitado) {
        BigDecimal valorAtual = dinheiro(investimento.getValorAtual());
        boolean total = valorSolicitado == null || valorSolicitado.compareTo(valorAtual) >= 0;
        BigDecimal valorBruto = total ? valorAtual : dinheiro(valorSolicitado);
        BigDecimal valorAplicado = dinheiro(investimento.getValorAplicado());
        BigDecimal rendimentoTotal = valorAtual.subtract(valorAplicado).max(ZERO);
        BigDecimal rendimentoProporcional = valorAtual.compareTo(BigDecimal.ZERO) <= 0
                ? ZERO
                : rendimentoTotal.multiply(valorBruto).divide(valorAtual, 8, RoundingMode.HALF_UP)
                        .setScale(2, RoundingMode.HALF_UP);
        BigDecimal imposto = rendimentoProporcional.multiply(aliquotaIr(investimento)).setScale(2, RoundingMode.HALF_UP);
        boolean antecipado = investimento.getDataVencimento() != null
                && LocalDate.now().isBefore(investimento.getDataVencimento())
                && investimento.getProdutoFinanceiro().getTipoProduto() != TipoProduto.ACAO;
        BigDecimal desconto = antecipado
                ? rendimentoProporcional.multiply(QUINZE_PORCENTO).setScale(2, RoundingMode.HALF_UP)
                        .min(rendimentoProporcional.subtract(imposto).max(ZERO))
                : ZERO;
        BigDecimal liquido = valorBruto.subtract(imposto).subtract(desconto).max(ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal remanescente = total ? ZERO : valorAtual.subtract(valorBruto).setScale(2, RoundingMode.HALF_UP);
        return new Calculo(valorAtual, valorBruto, imposto, desconto, liquido, remanescente, total, antecipado);
    }

    private BigDecimal aliquotaIr(Investimento investimento) {
        TipoProduto tipo = investimento.getProdutoFinanceiro().getTipoProduto();
        if (tipo == TipoProduto.LCI || tipo == TipoProduto.LCA || tipo == TipoProduto.POUPANCA) {
            return ZERO;
        }
        if (tipo == TipoProduto.ACAO) {
            return QUINZE_PORCENTO;
        }
        long dias = investimento.getDataAplicacao() == null
                ? 0
                : Math.max(0, ChronoUnit.DAYS.between(investimento.getDataAplicacao(), LocalDate.now()));
        if (dias <= 180) return VINTE_E_DOIS_E_MEIO_PORCENTO;
        if (dias <= 360) return VINTE_PORCENTO;
        if (dias <= 720) return DEZESSETE_E_MEIO_PORCENTO;
        return QUINZE_PORCENTO;
    }

    private BigDecimal novoValorAplicado(BigDecimal valorAplicado, BigDecimal remanescente, BigDecimal atual) {
        if (atual.compareTo(BigDecimal.ZERO) <= 0 || remanescente.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }
        return valorAplicado.multiply(remanescente).divide(atual, 8, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Investimento buscarDoUsuario(UUID investimentoId, Usuario usuario) {
        Investimento investimento = investimentoRepository.findById(investimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Investimento não encontrado."));
        validarProprietario(investimento, usuario);
        return investimento;
    }

    private void validarProprietario(Investimento investimento, Usuario usuario) {
        if (investimento.getUsuario() == null || !investimento.getUsuario().getId().equals(usuario.getId())) {
            throw new RecursoNaoEncontradoException("Investimento não encontrado.");
        }
    }

    private BigDecimal dinheiro(BigDecimal valor) {
        return valor == null ? ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private record ComandoResgate(UUID investimentoId, ResgateRequest request) {
    }

    private record Calculo(
            BigDecimal valorAtual,
            BigDecimal valorBruto,
            BigDecimal imposto,
            BigDecimal descontoAntecipacao,
            BigDecimal valorLiquido,
            BigDecimal saldoRemanescente,
            boolean total,
            boolean antecipado) {
    }
}
