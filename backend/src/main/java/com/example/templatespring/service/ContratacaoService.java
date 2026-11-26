package com.example.templatespring.service;

import com.example.templatespring.dto.contratacao.request.ContratacaoRequest;
import com.example.templatespring.dto.contratacao.request.PreviewContratacaoRequest;
import com.example.templatespring.dto.contratacao.response.ContratacaoResponse;
import com.example.templatespring.dto.contratacao.response.PreviewContratacaoResponse;
import com.example.templatespring.entity.Investimento;
import com.example.templatespring.entity.MovimentacaoSaldo;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.enums.StatusInvestimento;
import com.example.templatespring.entity.enums.TipoMovimentacaoSaldo;
import com.example.templatespring.entity.enums.TipoOperacaoFinanceira;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.repository.InvestimentoRepository;
import com.example.templatespring.repository.MovimentacaoSaldoRepository;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
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
public class ContratacaoService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final int DIAS_BASE_ANUAL = 365;

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoFinanceiroRepository produtoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final MovimentacaoSaldoRepository movimentacaoRepository;
    private final IdempotenciaService idempotenciaService;

    public ContratacaoService(
            UsuarioAutenticadoService usuarioAutenticadoService,
            UsuarioRepository usuarioRepository,
            ProdutoFinanceiroRepository produtoRepository,
            InvestimentoRepository investimentoRepository,
            MovimentacaoSaldoRepository movimentacaoRepository,
            IdempotenciaService idempotenciaService) {
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.investimentoRepository = investimentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.idempotenciaService = idempotenciaService;
    }

    @Transactional(readOnly = true)
    public PreviewContratacaoResponse visualizarPrevia(PreviewContratacaoRequest request) {
        Usuario usuario = usuarioAutenticadoService.get();
        ProdutoFinanceiro produto = produtoRepository.findById(request.getProdutoFinanceiroId()).orElse(null);
        return montarPrevia(usuario, produto, request.getValor());
    }

    public ContratacaoResponse contratar(ContratacaoRequest request, String chaveIdempotencia) {
        Usuario usuario = usuarioAutenticadoService.get();
        return idempotenciaService.executar(
                usuario.getId(),
                TipoOperacaoFinanceira.CONTRATACAO,
                chaveIdempotencia,
                request,
                ContratacaoResponse.class,
                () -> executarContratacao(usuario.getId(), request));
    }

    @Transactional
    protected ContratacaoResponse executarContratacao(UUID usuarioId, ContratacaoRequest request) {
        if (!Boolean.TRUE.equals(request.getConfirmar())) {
            throw new ResponseStatusException(BAD_REQUEST, "A confirmação da contratação é obrigatória.");
        }

        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Usuário não encontrado."));
        ProdutoFinanceiro produto = produtoRepository.findById(request.getProdutoFinanceiroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto financeiro não encontrado."));

        List<String> erros = validar(usuario, produto, request.getValor());
        if (!erros.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, String.join(" ", erros));
        }

        BigDecimal valor = dinheiro(request.getValor());
        BigDecimal saldoAntes = saldo(usuario.getSaldoDisponivel());
        BigDecimal saldoApos = saldoAntes.subtract(valor);
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();

        Investimento investimento = new Investimento();
        investimento.setUsuario(usuario);
        investimento.setProdutoFinanceiro(produto);
        investimento.setValorAplicado(valor);
        investimento.setValorAtual(valor);
        investimento.setDataAplicacao(hoje);
        investimento.setDataVencimento(produto.getPrazoDias() == null
                ? null
                : hoje.plusDays(produto.getPrazoDias()));
        investimento.setStatus(StatusInvestimento.ATIVO);
        investimento.setTaxaContratada(taxaAnualDecimal(produto));
        investimento.setIndexadorContratado(produto.getIndexador());
        investimento.setPercentualIndexadorContratado(produto.getPercentualIndexador());
        investimento.setCarenciaDiasContratada(produto.getCarenciaDias() == null ? 0 : produto.getCarenciaDias());
        investimento.setCreatedAt(agora);
        investimento.setUpdatedAt(agora);
        Investimento salvo = investimentoRepository.save(investimento);

        usuario.setSaldoDisponivel(saldoApos);
        usuarioRepository.save(usuario);

        MovimentacaoSaldo movimentacao = new MovimentacaoSaldo();
        movimentacao.setUsuario(usuario);
        movimentacao.setInvestimento(salvo);
        movimentacao.setTipo(TipoMovimentacaoSaldo.CONTRATACAO);
        movimentacao.setValor(valor.negate());
        movimentacao.setSaldoAntes(saldoAntes);
        movimentacao.setSaldoApos(saldoApos);
        movimentacao.setDataMovimentacao(agora);
        movimentacaoRepository.save(movimentacao);

        PreviaCalculo calculo = calcular(produto, valor);
        return ContratacaoResponse.builder()
                .investimentoId(salvo.getId())
                .produtoFinanceiroId(produto.getId())
                .nomeProduto(produto.getNome())
                .valorAplicado(valor)
                .saldoDisponivel(saldoApos)
                .valorBrutoEstimado(calculo.valorBruto())
                .impostoEstimado(calculo.imposto())
                .valorLiquidoEstimado(calculo.valorLiquido())
                .taxaAnualPercentual(produto.getRentabilidadePercentual())
                .nivelRisco(produto.getNivelRisco())
                .liquidez(produto.getLiquidez())
                .carenciaDias(produto.getCarenciaDias())
                .prazoDias(produto.getPrazoDias())
                .dataVencimento(investimento.getDataVencimento())
                .contratadoEm(agora)
                .build();
    }

    private PreviewContratacaoResponse montarPrevia(Usuario usuario, ProdutoFinanceiro produto, BigDecimal valorInformado) {
        BigDecimal valor = valorInformado == null ? null : dinheiro(valorInformado);
        List<String> erros = produto == null
                ? new ArrayList<>(List.of("Produto financeiro não encontrado."))
                : validar(usuario, produto, valorInformado);

        if (produto == null) {
            return PreviewContratacaoResponse.builder()
                    .validoParaConfirmacao(false)
                    .erros(erros)
                    .saldoDisponivel(saldo(usuario.getSaldoDisponivel()))
                    .build();
        }

        PreviaCalculo calculo = valor == null ? null : calcular(produto, valor);
        return PreviewContratacaoResponse.builder()
                .validoParaConfirmacao(erros.isEmpty())
                .erros(erros)
                .produtoFinanceiroId(produto.getId())
                .nomeProduto(produto.getNome())
                .valor(valor)
                .valorMinimo(dinheiro(produto.getValorMinimo()))
                .saldoDisponivel(saldo(usuario.getSaldoDisponivel()))
                .taxaAnualPercentual(produto.getRentabilidadePercentual())
                .valorBrutoEstimado(calculo == null ? null : calculo.valorBruto())
                .impostoEstimado(calculo == null ? null : calculo.imposto())
                .valorLiquidoEstimado(calculo == null ? null : calculo.valorLiquido())
                .taxas(produto.getTaxas())
                .tributacao(produto.getTributacao())
                .nivelRisco(produto.getNivelRisco())
                .liquidez(produto.getLiquidez())
                .carenciaDias(produto.getCarenciaDias())
                .prazoDias(produto.getPrazoDias())
                .dataVencimento(produto.getPrazoDias() == null ? null : LocalDate.now().plusDays(produto.getPrazoDias()))
                .build();
    }

    private List<String> validar(Usuario usuario, ProdutoFinanceiro produto, BigDecimal valor) {
        List<String> erros = new ArrayList<>();
        if (!produto.isAtivo()) {
            erros.add("O produto está inativo e não pode ser contratado.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            erros.add("Informe um valor maior que zero.");
            return erros;
        }
        if (valor.scale() > 2) {
            erros.add("O valor deve possuir no máximo duas casas decimais.");
        }
        if (produto.getValorMinimo() != null && valor.compareTo(produto.getValorMinimo()) < 0) {
            erros.add("O valor informado é menor que o mínimo de " + dinheiro(produto.getValorMinimo()) + ".");
        }
        if (saldo(usuario.getSaldoDisponivel()).compareTo(dinheiro(valor)) < 0) {
            erros.add("Saldo disponível insuficiente para esta contratação.");
        }
        if (!produtoAdequadoAoPerfil(usuario, produto)) {
            erros.add("O produto não é compatível com o perfil de investidor informado.");
        }
        return erros;
    }

    private boolean produtoAdequadoAoPerfil(Usuario usuario, ProdutoFinanceiro produto) {
        String risco = produto.getNivelRisco() == null ? "" : produto.getNivelRisco().trim().toUpperCase();
        PerfilInvestidor perfil = usuario.getPerfilInvestidor() == null
                ? PerfilInvestidor.CONSERVADOR
                : usuario.getPerfilInvestidor();
        return switch (perfil) {
            case CONSERVADOR -> "BAIXO".equals(risco);
            case MODERADO -> !"ALTO".equals(risco);
            case ARROJADO -> true;
        };
    }

    private PreviaCalculo calcular(ProdutoFinanceiro produto, BigDecimal valor) {
        int prazoDias = produto.getPrazoDias() == null ? 0 : Math.max(produto.getPrazoDias(), 0);
        BigDecimal taxaAnual = taxaAnualDecimal(produto);
        double fator = Math.pow(1.0d + taxaAnual.doubleValue(), prazoDias / (double) DIAS_BASE_ANUAL);
        BigDecimal bruto = valor.multiply(BigDecimal.valueOf(fator)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal lucro = bruto.subtract(valor).max(ZERO);
        BigDecimal imposto = lucro.multiply(aliquotaIr(produto, prazoDias)).setScale(2, RoundingMode.HALF_UP);
        return new PreviaCalculo(bruto, imposto, bruto.subtract(imposto).setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal taxaAnualDecimal(ProdutoFinanceiro produto) {
        return produto.getRentabilidadePercentual() == null
                ? BigDecimal.ZERO
                : produto.getRentabilidadePercentual().divide(CEM, 8, RoundingMode.HALF_UP);
    }

    private BigDecimal aliquotaIr(ProdutoFinanceiro produto, int prazoDias) {
        String tipo = produto.getTipoProduto() == null ? "" : produto.getTipoProduto().name();
        if ("LCI".equals(tipo) || "LCA".equals(tipo) || "POUPANCA".equals(tipo)) {
            return BigDecimal.ZERO;
        }
        if (prazoDias <= 180) return new BigDecimal("0.225");
        if (prazoDias <= 360) return new BigDecimal("0.20");
        if (prazoDias <= 720) return new BigDecimal("0.175");
        return new BigDecimal("0.15");
    }

    private BigDecimal dinheiro(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal saldo(BigDecimal valor) {
        return dinheiro(valor == null ? ZERO : valor);
    }

    private record PreviaCalculo(BigDecimal valorBruto, BigDecimal imposto, BigDecimal valorLiquido) {
    }
}
