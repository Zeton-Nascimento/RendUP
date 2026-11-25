package com.example.templatespring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.templatespring.dto.simulacao.request.SimulacaoRequest;
import com.example.templatespring.dto.simulacao.response.CenarioResponse;
import com.example.templatespring.dto.simulacao.response.SimulacaoHistoricoResponse;
import com.example.templatespring.dto.simulacao.response.SimulacaoResponse;
import com.example.templatespring.dto.simulacao.response.SimulacaoResumoCenarioResponse;
import com.example.templatespring.dto.simulacao.response.SimulacaoResumoResponse;
import com.example.templatespring.entity.CenarioSimulacao;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.entity.Simulacao;
import com.example.templatespring.entity.enums.OrigemSimulacao;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.enums.TipoDadoSimulacao;
import com.example.templatespring.repository.CenarioSimulacaoRepository;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import com.example.templatespring.repository.SimulacaoRepository;
import com.example.templatespring.validator.SimulacaoValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.templatespring.exception.RecursoNaoEncontradoException;

@Service
public class SimulacaoService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final String FONTE_DADOS = "Catálogo local mockado do RendUP";
    private static final String METODOLOGIA =
            "A taxa anual do produto é convertida para uma taxa mensal equivalente. "
                    + "O aporte inicial rende mensalmente e o aporte mensal é acrescentado ao final de cada mês. "
                    + "O valor líquido aplica a regra simplificada de IR conforme o prazo e o tipo do produto. "
                    + "Os valores monetários são arredondados para duas casas decimais.";
    private static final String LIMITACAO =
            "Os dados de mercado são mockados e os valores são estimativas. "
                    + "O resultado não é garantia de rentabilidade, recomendação ou base suficiente para uma decisão de investimento.";
    private static final TypeReference<List<BigDecimal>> EVOLUCAO_TYPE = new TypeReference<>() {};

    private final SimulacaoValidator validator;
    private final ProdutoFinanceiroRepository produtoRepository;
    private final SimulacaoRepository simulacaoRepository;
    private final CenarioSimulacaoRepository cenarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ObjectMapper objectMapper;

    public SimulacaoService(
            SimulacaoValidator validator,
            ProdutoFinanceiroRepository produtoRepository,
            SimulacaoRepository simulacaoRepository,
            CenarioSimulacaoRepository cenarioRepository,
            UsuarioAutenticadoService usuarioAutenticadoService,
            ObjectMapper objectMapper) {
        this.validator = validator;
        this.produtoRepository = produtoRepository;
        this.simulacaoRepository = simulacaoRepository;
        this.cenarioRepository = cenarioRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SimulacaoResponse simular(SimulacaoRequest request) {
        return simularComOrigem(request, OrigemSimulacao.MANUAL);
    }

    @Transactional
    public SimulacaoResponse simularComOrigem(SimulacaoRequest request, OrigemSimulacao origem) {
        validator.validar(request);
        var usuario = usuarioAutenticadoService.get();

        List<UUID> ids = request.getProdutoIds();
        Map<UUID, ProdutoFinanceiro> produtos = new HashMap<>();
        produtoRepository.findAllById(ids).forEach(produto -> produtos.put(produto.getId(), produto));

        List<CenarioCalculado> calculados = ids.stream()
                .map(id -> calcular(produtos.get(id), request))
                .sorted(comparadorParaPerfil(request.getPerfil()))
                .toList();

        Simulacao simulacao = new Simulacao();
        simulacao.setUsuario(usuario);
        simulacao.setOrigem(origem);
        simulacao.setPerfil(request.getPerfil());
        simulacao.setCreatedAt(LocalDateTime.now());
        Simulacao salva = simulacaoRepository.save(simulacao);

        List<CenarioSimulacao> cenarios = new ArrayList<>();
        for (int index = 0; index < calculados.size(); index++) {
            CenarioCalculado calculado = calculados.get(index);
            CenarioSimulacao cenario = new CenarioSimulacao();
            cenario.setSimulacao(salva);
            cenario.setProdutoFinanceiro(calculado.produto());
            cenario.setTipoProduto(calculado.produto().getTipoProduto());
            cenario.setValorInicial(request.getValorInicial().setScale(2, RoundingMode.HALF_UP));
            cenario.setAporteMensal(request.getAporteMensal().setScale(2, RoundingMode.HALF_UP));
            cenario.setPrazoMeses(request.getPrazoMeses());
            cenario.setValorFinalBruto(calculado.valorFinalBruto());
            cenario.setValorFinalLiquido(calculado.valorFinalLiquido());
            cenario.setRentabilidadePercentual(calculado.rentabilidade());
            cenario.setEvolucaoMensal(serializar(calculado.evolucaoMensal()));
            cenario.setRankingPosicao(index + 1);
            cenario.setDestaque(index == 0 ? destaqueParaPerfil(request.getPerfil()) : null);
            cenarios.add(cenario);
        }

        return resposta(salva, cenarioRepository.saveAll(cenarios));
    }

    @Transactional(readOnly = true)
    public SimulacaoResponse buscarPorId(UUID id) {
        var usuario = usuarioAutenticadoService.get();
        Simulacao simulacao = simulacaoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Simulação não encontrada."));
        return resposta(simulacao, cenarioRepository.findBySimulacaoIdOrderByRankingPosicaoAsc(id));
    }

    @Transactional(readOnly = true)
    public List<SimulacaoHistoricoResponse> historico() {
        var usuario = usuarioAutenticadoService.get();
        return simulacaoRepository.findByUsuarioIdOrderByCreatedAtDesc(usuario.getId()).stream()
                .map(this::historicoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SimulacaoResumoResponse resumoCompartilhavel(UUID id) {
        SimulacaoResponse simulacao = buscarPorId(id);
        List<CenarioResponse> cenarios = simulacao.getCenarios();
        CenarioResponse primeiro = cenarios.get(0);
        PerfilInvestidor perfil = perfilDa(simulacao.getPerfil());
        String data = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(simulacao.getCreatedAt());
        String origem = simulacao.getOrigem() == OrigemSimulacao.ASSISTENTE_IA
                ? "Assistente de IA"
                : "Solicitação manual";

        List<SimulacaoResumoCenarioResponse> resumoCenarios = cenarios.stream()
                .map(cenario -> SimulacaoResumoCenarioResponse.builder()
                        .produto(cenario.getNomeProduto())
                        .tipoProduto(cenario.getTipoProduto())
                        .valorFinalLiquido(cenario.getValorFinalLiquido())
                        .rentabilidade(cenario.getRentabilidade())
                        .liquidez(cenario.getLiquidez())
                        .nivelRisco(cenario.getNivelRisco())
                        .build())
                .toList();

        StringBuilder texto = new StringBuilder();
        texto.append("Resumo de simulação RendUP\n")
                .append("Data: ").append(data).append("\n")
                .append("Origem: ").append(origem).append("\n")
                .append("Perfil: ").append(perfil.name()).append("\n")
                .append("Valor inicial: R$ ").append(primeiro.getValorInicial()).append("\n")
                .append("Aporte mensal: R$ ").append(primeiro.getAporteMensal()).append("\n")
                .append("Prazo: ").append(primeiro.getPrazoMeses()).append(" meses\n\n")
                .append("Cenários:\n");
        for (SimulacaoResumoCenarioResponse cenario : resumoCenarios) {
            texto.append("- ").append(cenario.getProduto())
                    .append(" | líquido: R$ ").append(cenario.getValorFinalLiquido())
                    .append(" | rentabilidade: ").append(cenario.getRentabilidade()).append("%")
                    .append(" | liquidez: ").append(cenario.getLiquidez())
                    .append(" | risco: ").append(cenario.getNivelRisco()).append("\n");
        }
        texto.append("\nOs dados são estimativas mockadas e não constituem recomendação de investimento.");

        return SimulacaoResumoResponse.builder()
                .id(simulacao.getId())
                .createdAt(simulacao.getCreatedAt())
                .origem(simulacao.getOrigem())
                .perfil(perfil)
                .valorInicial(primeiro.getValorInicial())
                .aporteMensal(primeiro.getAporteMensal())
                .prazoMeses(primeiro.getPrazoMeses())
                .cenarios(resumoCenarios)
                .textoCompartilhavel(texto.toString())
                .aviso("O resumo não contém dados pessoais, credenciais, tokens ou dados bancários.")
                .build();
    }

    private SimulacaoHistoricoResponse historicoResponse(Simulacao simulacao) {
        List<CenarioSimulacao> cenarios = cenarioRepository.findBySimulacaoIdOrderByRankingPosicaoAsc(simulacao.getId());
        CenarioSimulacao primeiro = cenarios.isEmpty() ? null : cenarios.get(0);
        return SimulacaoHistoricoResponse.builder()
                .id(simulacao.getId())
                .createdAt(simulacao.getCreatedAt())
                .origem(simulacao.getOrigem())
                .perfil(perfilDa(simulacao.getPerfil()))
                .valorInicial(primeiro == null ? null : primeiro.getValorInicial())
                .aporteMensal(primeiro == null ? null : primeiro.getAporteMensal())
                .prazoMeses(primeiro == null ? null : primeiro.getPrazoMeses())
                .quantidadeCenarios(cenarios.size())
                .melhorProduto(primeiro == null ? null : primeiro.getProdutoFinanceiro().getNome())
                .melhorValorFinalLiquido(primeiro == null ? null : primeiro.getValorFinalLiquido())
                .build();
    }

    private PerfilInvestidor perfilDa(PerfilInvestidor perfil) {
        return perfil == null ? PerfilInvestidor.CONSERVADOR : perfil;
    }

    private CenarioCalculado calcular(ProdutoFinanceiro produto, SimulacaoRequest request) {
        BigDecimal taxaMensal = produto.getRentabilidadePercentual()
                .divide(CEM, 8, RoundingMode.HALF_UP);
        taxaMensal = BigDecimal.valueOf(Math.pow(BigDecimal.ONE.add(taxaMensal).doubleValue(), 1.0 / 12.0) - 1.0)
                .setScale(8, RoundingMode.HALF_UP);

        BigDecimal saldo = request.getValorInicial().setScale(8, RoundingMode.HALF_UP);
        List<BigDecimal> evolucao = new ArrayList<>();
        for (int mes = 0; mes < request.getPrazoMeses(); mes++) {
            saldo = saldo.multiply(BigDecimal.ONE.add(taxaMensal)).setScale(8, RoundingMode.HALF_UP);
            saldo = saldo.add(request.getAporteMensal()).setScale(8, RoundingMode.HALF_UP);
            evolucao.add(saldo.setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal totalAplicado = request.getValorInicial()
                .add(request.getAporteMensal().multiply(BigDecimal.valueOf(request.getPrazoMeses())))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal bruto = saldo.setScale(2, RoundingMode.HALF_UP);
        BigDecimal imposto = imposto(bruto.subtract(totalAplicado), produto, request.getPrazoMeses());
        BigDecimal liquido = bruto.subtract(imposto).setScale(2, RoundingMode.HALF_UP);
        BigDecimal rentabilidade = liquido.subtract(totalAplicado)
                .multiply(CEM)
                .divide(totalAplicado, 2, RoundingMode.HALF_UP);
        return new CenarioCalculado(produto, bruto, liquido, rentabilidade, evolucao);
    }

    private BigDecimal imposto(BigDecimal lucro, ProdutoFinanceiro produto, int prazoMeses) {
        if (lucro.compareTo(BigDecimal.ZERO) <= 0 || produto.getTipoProduto().name().equals("LCI")
                || produto.getTipoProduto().name().equals("LCA")
                || produto.getTipoProduto().name().equals("POUPANCA")) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal aliquota = prazoMeses <= 6 ? new BigDecimal("0.225")
                : prazoMeses <= 12 ? new BigDecimal("0.20")
                : prazoMeses <= 24 ? new BigDecimal("0.175") : new BigDecimal("0.15");
        return lucro.multiply(aliquota).setScale(2, RoundingMode.HALF_UP);
    }

    private Comparator<CenarioCalculado> comparadorParaPerfil(PerfilInvestidor perfil) {
        return switch (perfil) {
            case CONSERVADOR -> Comparator
                    .comparingInt((CenarioCalculado cenario) -> prioridadeRisco(cenario.produto()))
                    .thenComparing(Comparator.comparingInt(
                            (CenarioCalculado cenario) -> prioridadeLiquidez(cenario.produto())).reversed())
                    .thenComparing(CenarioCalculado::valorFinalLiquido, Comparator.reverseOrder());
            case MODERADO -> Comparator
                    .comparing(this::pontuacaoModerada, Comparator.reverseOrder())
                    .thenComparing(CenarioCalculado::valorFinalLiquido, Comparator.reverseOrder());
            case ARROJADO -> Comparator
                    .comparing(CenarioCalculado::rentabilidade, Comparator.reverseOrder())
                    .thenComparing(CenarioCalculado::valorFinalLiquido, Comparator.reverseOrder());
        };
    }

    private BigDecimal pontuacaoModerada(CenarioCalculado cenario) {
        BigDecimal penalidadeRisco = switch (prioridadeRisco(cenario.produto())) {
            case 1 -> BigDecimal.ZERO;
            case 2 -> new BigDecimal("2");
            default -> new BigDecimal("6");
        };
        BigDecimal bonusLiquidez = BigDecimal.valueOf(prioridadeLiquidez(cenario.produto()))
                .multiply(new BigDecimal("0.25"));
        return cenario.rentabilidade().subtract(penalidadeRisco).add(bonusLiquidez);
    }

    private int prioridadeRisco(ProdutoFinanceiro produto) {
        return switch (produto.getNivelRisco()) {
            case "BAIXO" -> 1;
            case "MEDIO" -> 2;
            case "ALTO" -> 3;
            default -> 4;
        };
    }

    private int prioridadeLiquidez(ProdutoFinanceiro produto) {
        return switch (produto.getLiquidez()) {
            case DIARIA -> 3;
            case D_PLUS_N -> 2;
            case NO_VENCIMENTO -> 1;
        };
    }

    private String destaqueParaPerfil(PerfilInvestidor perfil) {
        return switch (perfil) {
            case CONSERVADOR -> "Mais adequado ao perfil conservador";
            case MODERADO -> "Mais adequado ao perfil moderado";
            case ARROJADO -> "Mais adequado ao perfil arrojado";
        };
    }

    private SimulacaoResponse resposta(Simulacao simulacao, List<CenarioSimulacao> entidades) {
        return SimulacaoResponse.builder()
                .id(simulacao.getId())
                .origem(simulacao.getOrigem())
                .perfil(perfilDa(simulacao.getPerfil()))
                .fonteDados(FONTE_DADOS)
                .tipoDado(TipoDadoSimulacao.MOCKADO)
                .dataReferencia(simulacao.getCreatedAt())
                .metodologia(METODOLOGIA)
                .limitacao(LIMITACAO)
                .disponivelParaDecisao(false)
                .createdAt(simulacao.getCreatedAt())
                .cenarios(entidades.stream().map(this::cenarioResponse).toList())
                .build();
    }

    private CenarioResponse cenarioResponse(CenarioSimulacao cenario) {
        return CenarioResponse.builder()
                .produtoId(cenario.getProdutoFinanceiro().getId())
                .nomeProduto(cenario.getProdutoFinanceiro().getNome())
                .tipoProduto(cenario.getTipoProduto())
                .valorFinalBruto(cenario.getValorFinalBruto())
                .valorFinalLiquido(cenario.getValorFinalLiquido())
                .rentabilidade(cenario.getRentabilidadePercentual())
                .liquidez(cenario.getProdutoFinanceiro().getLiquidez())
                .nivelRisco(cenario.getProdutoFinanceiro().getNivelRisco())
                .evolucaoMensal(desserializar(cenario.getEvolucaoMensal()))
                .ranking(cenario.getRankingPosicao())
                .destaque(cenario.getDestaque())
                .valorInicial(cenario.getValorInicial())
                .aporteMensal(cenario.getAporteMensal())
                .prazoMeses(cenario.getPrazoMeses())
                .build();
    }

    private String serializar(List<BigDecimal> valores) {
        try {
            return objectMapper.writeValueAsString(valores);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível guardar a evolução da simulação.", exception);
        }
    }

    private List<BigDecimal> desserializar(String valores) {
        try {
            return objectMapper.readValue(valores, EVOLUCAO_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível ler a evolução da simulação.", exception);
        }
    }

    private record CenarioCalculado(
            ProdutoFinanceiro produto,
            BigDecimal valorFinalBruto,
            BigDecimal valorFinalLiquido,
            BigDecimal rentabilidade,
            List<BigDecimal> evolucaoMensal) {}
}
