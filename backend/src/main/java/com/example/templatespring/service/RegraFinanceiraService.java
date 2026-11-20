package com.example.templatespring.service;

import com.example.templatespring.dto.regra.CenarioReferenciaRegraRequest;
import com.example.templatespring.dto.regra.RegraFinanceiraEventoRequest;
import com.example.templatespring.dto.regra.RegraFinanceiraRequest;
import com.example.templatespring.dto.regra.RegraFinanceiraResponse;
import com.example.templatespring.dto.regra.ValidacaoRegraFinanceiraResponse;
import com.example.templatespring.entity.CenarioReferenciaRegra;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.entity.RegraFinanceira;
import com.example.templatespring.entity.RegraFinanceiraEvento;
import com.example.templatespring.entity.enums.EventoFinanceiro;
import com.example.templatespring.entity.enums.StatusRegraFinanceira;
import com.example.templatespring.entity.enums.TipoProduto;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.repository.RegraFinanceiraRepository;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegraFinanceiraService {

    private static final Set<EventoFinanceiro> EVENTOS_OBRIGATORIOS =
            EnumSet.allOf(EventoFinanceiro.class);

    private final RegraFinanceiraRepository repository;
    private final ProdutoFinanceiroRepository produtoRepository;

    public RegraFinanceiraService(
            RegraFinanceiraRepository repository,
            ProdutoFinanceiroRepository produtoRepository) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<RegraFinanceiraResponse> listar() {
        return repository.findAllByOrderByTipoProdutoAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegraFinanceiraResponse buscar(TipoProduto tipoProduto) {
        return repository.findByTipoProduto(tipoProduto)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma regra financeira foi documentada para o produto " + tipoProduto + "."));
    }

    @Transactional
    public RegraFinanceiraResponse salvar(TipoProduto tipoProduto, RegraFinanceiraRequest request) {
        if (request == null || request.getTipoProduto() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "O tipo do produto é obrigatório.");
        }
        if (tipoProduto != request.getTipoProduto()) {
            throw erro(HttpStatus.BAD_REQUEST, "O tipo do produto da URL deve ser igual ao tipo enviado.");
        }

        validarEventosRecebidos(request.getEventos());
        validarProdutosReferenciados(tipoProduto, request.getEventos());
        List<String> pendencias = calcularPendencias(request.getStatus(), request.getEventos());
        if (request.getStatus() == StatusRegraFinanceira.APROVADA && !pendencias.isEmpty()) {
            throw erro(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A regra não pode ser aprovada enquanto houver pendências: " + String.join(" ", pendencias));
        }

        RegraFinanceira regra = repository.findByTipoProduto(tipoProduto).orElseGet(RegraFinanceira::new);
        if (regra.getId() == null) {
            regra.setCreatedAt(LocalDateTime.now());
        }
        regra.setTipoProduto(tipoProduto);
        regra.setVersao(normalizarObrigatorio(request.getVersao(), "versao"));
        regra.setStatus(request.getStatus());
        regra.setObservacoes(normalizar(request.getObservacoes()));
        regra.setUpdatedAt(LocalDateTime.now());
        regra.substituirEventos(toEntities(request.getEventos()));

        return toResponse(repository.save(regra));
    }

    @Transactional(readOnly = true)
    public ValidacaoRegraFinanceiraResponse validar(TipoProduto tipoProduto) {
        Optional<RegraFinanceira> encontrada = repository.findByTipoProduto(tipoProduto);
        if (encontrada.isEmpty()) {
            return ValidacaoRegraFinanceiraResponse.builder()
                    .tipoProduto(tipoProduto)
                    .statusDocumento("AUSENTE")
                    .podeProcessar(false)
                    .pendencias(List.of("Nenhuma regra financeira foi documentada para este tipo de produto."))
                    .build();
        }

        RegraFinanceira regra = encontrada.get();
        List<String> pendencias = calcularPendencias(regra);
        boolean podeProcessar = regra.getStatus() == StatusRegraFinanceira.APROVADA && pendencias.isEmpty();
        return ValidacaoRegraFinanceiraResponse.builder()
                .tipoProduto(tipoProduto)
                .statusDocumento(regra.getStatus().name())
                .podeProcessar(podeProcessar)
                .pendencias(pendencias)
                .build();
    }

    /**
     * Guard de domínio para os fluxos financeiros que serão implementados depois.
     * Uma operação não deve prosseguir sem uma regra aprovada e completa.
     */
    @Transactional(readOnly = true)
    public void exigirRegraAprovada(TipoProduto tipoProduto) {
        ValidacaoRegraFinanceiraResponse validacao = validar(tipoProduto);
        if (!validacao.isPodeProcessar()) {
            throw erro(HttpStatus.CONFLICT,
                    "Processamento bloqueado: " + String.join(" ", validacao.getPendencias()));
        }
    }

    private void validarEventosRecebidos(List<RegraFinanceiraEventoRequest> eventos) {
        if (eventos == null || eventos.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe as regras dos eventos financeiros.");
        }

        Set<EventoFinanceiro> eventosEncontrados = new HashSet<>();
        for (RegraFinanceiraEventoRequest evento : eventos) {
            if (evento == null || evento.getEvento() == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Cada regra deve informar um evento financeiro.");
            }
            if (!eventosEncontrados.add(evento.getEvento())) {
                throw erro(HttpStatus.CONFLICT,
                        "Existe mais de uma regra documentada para o evento " + evento.getEvento() + ".");
            }
        }
    }

    private List<String> calcularPendencias(RegraFinanceira regra) {
        if (regra.getStatus() == StatusRegraFinanceira.PENDENTE) {
            return List.of("O documento está pendente de aprovação.");
        }
        if (regra.getStatus() == StatusRegraFinanceira.CONFLITANTE) {
            return List.of("Há conflito entre as regras documentadas.");
        }

        List<RegraFinanceiraEventoRequest> eventos = regra.getEventos().stream()
                .map(this::toRequest)
                .toList();
        return calcularPendencias(regra.getStatus(), eventos);
    }

    private List<String> calcularPendencias(
            StatusRegraFinanceira status,
            List<RegraFinanceiraEventoRequest> eventos) {
        List<String> pendencias = new ArrayList<>();
        Set<EventoFinanceiro> encontrados = eventos == null
                ? Set.of()
                : eventos.stream()
                        .filter(evento -> evento != null && evento.getEvento() != null)
                        .map(RegraFinanceiraEventoRequest::getEvento)
                        .collect(java.util.stream.Collectors.toSet());

        for (EventoFinanceiro obrigatorio : EVENTOS_OBRIGATORIOS) {
            if (!encontrados.contains(obrigatorio)) {
                pendencias.add("Falta documentar o evento " + obrigatorio + ".");
            }
        }

        if (eventos != null) {
            for (RegraFinanceiraEventoRequest evento : eventos) {
                if (evento == null || evento.getEvento() == null) {
                    continue;
                }
                if (normalizar(evento.getDescricao()) == null) {
                    pendencias.add("O evento " + evento.getEvento() + " não possui descrição.");
                }
                if (normalizar(evento.getFormula()) == null) {
                    pendencias.add("O evento " + evento.getEvento() + " não possui fórmula ou regra de cálculo.");
                }
                if (evento.getCenariosReferencia() == null || evento.getCenariosReferencia().isEmpty()) {
                    pendencias.add("O evento " + evento.getEvento() + " não possui cenário de referência.");
                } else {
                    for (CenarioReferenciaRegraRequest cenario : evento.getCenariosReferencia()) {
                        if (cenario == null || cenario.getProdutoId() == null) {
                            pendencias.add(
                                    "O cenário do evento " + evento.getEvento() + " não identifica um produto.");
                        }
                    }
                }
            }
        }
        return pendencias;
    }

    private List<RegraFinanceiraEvento> toEntities(List<RegraFinanceiraEventoRequest> requests) {
        return requests.stream().map(request -> {
            RegraFinanceiraEvento evento = new RegraFinanceiraEvento();
            evento.setEvento(request.getEvento());
            evento.setDescricao(normalizarObrigatorio(request.getDescricao(), "descricao"));
            evento.setFormula(normalizarObrigatorio(request.getFormula(), "formula"));
            if (request.getCenariosReferencia() != null) {
                request.getCenariosReferencia().forEach(cenarioRequest -> evento.adicionarCenario(toEntity(cenarioRequest)));
            }
            return evento;
        }).toList();
    }

    private CenarioReferenciaRegra toEntity(CenarioReferenciaRegraRequest request) {
        CenarioReferenciaRegra cenario = new CenarioReferenciaRegra();
        if (request.getProdutoId() != null) {
            cenario.setProduto(produtoRepository.findById(request.getProdutoId()).orElseThrow(() ->
                    erro(HttpStatus.BAD_REQUEST, "Produto do cenário de referência não encontrado.")));
        }
        cenario.setNome(normalizarObrigatorio(request.getNome(), "nome do cenário"));
        cenario.setValorEntrada(request.getValorEntrada());
        cenario.setAporteMensal(request.getAporteMensal());
        cenario.setPrazoDias(request.getPrazoDias());
        cenario.setTaxaPercentual(request.getTaxaPercentual());
        cenario.setValorBrutoEsperado(request.getValorBrutoEsperado());
        cenario.setImpostoEsperado(request.getImpostoEsperado());
        cenario.setValorLiquidoEsperado(request.getValorLiquidoEsperado());
        cenario.setValorArredondadoEsperado(request.getValorArredondadoEsperado());
        cenario.setCasasDecimais(request.getCasasDecimais());
        cenario.setObservacoes(normalizar(request.getObservacoes()));
        return cenario;
    }

    private RegraFinanceiraEventoRequest toRequest(RegraFinanceiraEvento evento) {
        RegraFinanceiraEventoRequest request = new RegraFinanceiraEventoRequest();
        request.setEvento(evento.getEvento());
        request.setDescricao(evento.getDescricao());
        request.setFormula(evento.getFormula());
        request.setCenariosReferencia(evento.getCenariosReferencia().stream().map(this::toRequest).toList());
        return request;
    }

    private CenarioReferenciaRegraRequest toRequest(CenarioReferenciaRegra cenario) {
        CenarioReferenciaRegraRequest request = new CenarioReferenciaRegraRequest();
        request.setProdutoId(cenario.getProduto() == null ? null : cenario.getProduto().getId());
        request.setNome(cenario.getNome());
        request.setValorEntrada(cenario.getValorEntrada());
        request.setAporteMensal(cenario.getAporteMensal());
        request.setPrazoDias(cenario.getPrazoDias());
        request.setTaxaPercentual(cenario.getTaxaPercentual());
        request.setValorBrutoEsperado(cenario.getValorBrutoEsperado());
        request.setImpostoEsperado(cenario.getImpostoEsperado());
        request.setValorLiquidoEsperado(cenario.getValorLiquidoEsperado());
        request.setValorArredondadoEsperado(cenario.getValorArredondadoEsperado());
        request.setCasasDecimais(cenario.getCasasDecimais());
        request.setObservacoes(cenario.getObservacoes());
        return request;
    }

    private RegraFinanceiraResponse toResponse(RegraFinanceira regra) {
        List<String> pendencias = calcularPendencias(regra);
        return RegraFinanceiraResponse.builder()
                .id(regra.getId())
                .tipoProduto(regra.getTipoProduto())
                .versao(regra.getVersao())
                .status(regra.getStatus())
                .observacoes(regra.getObservacoes())
                .createdAt(regra.getCreatedAt())
                .updatedAt(regra.getUpdatedAt())
                .prontaParaAplicacao(regra.getStatus() == StatusRegraFinanceira.APROVADA && pendencias.isEmpty())
                .pendencias(pendencias)
                .eventos(regra.getEventos().stream().map(this::toResponse).toList())
                .build();
    }

    private RegraFinanceiraResponse.RegraFinanceiraEventoResponse toResponse(RegraFinanceiraEvento evento) {
        return RegraFinanceiraResponse.RegraFinanceiraEventoResponse.builder()
                .evento(evento.getEvento())
                .descricao(evento.getDescricao())
                .formula(evento.getFormula())
                .cenariosReferencia(evento.getCenariosReferencia().stream().map(this::toResponse).toList())
                .build();
    }

    private RegraFinanceiraResponse.CenarioReferenciaResponse toResponse(CenarioReferenciaRegra cenario) {
        return RegraFinanceiraResponse.CenarioReferenciaResponse.builder()
                .id(cenario.getId())
                .produtoId(cenario.getProduto() == null ? null : cenario.getProduto().getId())
                .produtoNome(cenario.getProduto() == null ? null : cenario.getProduto().getNome())
                .nome(cenario.getNome())
                .valorEntrada(cenario.getValorEntrada())
                .aporteMensal(cenario.getAporteMensal())
                .prazoDias(cenario.getPrazoDias())
                .taxaPercentual(cenario.getTaxaPercentual())
                .valorBrutoEsperado(cenario.getValorBrutoEsperado())
                .impostoEsperado(cenario.getImpostoEsperado())
                .valorLiquidoEsperado(cenario.getValorLiquidoEsperado())
                .valorArredondadoEsperado(cenario.getValorArredondadoEsperado())
                .casasDecimais(cenario.getCasasDecimais())
                .observacoes(cenario.getObservacoes())
                .build();
    }

    private void validarProdutosReferenciados(
            TipoProduto tipoProduto,
            List<RegraFinanceiraEventoRequest> eventos) {
        if (eventos == null) {
            return;
        }

        for (RegraFinanceiraEventoRequest evento : eventos) {
            Set<UUID> produtosDoEvento = new HashSet<>();
            if (evento == null || evento.getCenariosReferencia() == null) {
                continue;
            }
            for (CenarioReferenciaRegraRequest cenario : evento.getCenariosReferencia()) {
                if (cenario == null || cenario.getProdutoId() == null) {
                    continue;
                }
                if (!produtosDoEvento.add(cenario.getProdutoId())) {
                    throw erro(HttpStatus.CONFLICT,
                            "O mesmo produto não pode aparecer duas vezes no evento " + evento.getEvento() + ".");
                }
                ProdutoFinanceiro produto = produtoRepository.findById(cenario.getProdutoId())
                        .orElseThrow(() -> erro(
                                HttpStatus.BAD_REQUEST,
                                "Produto do cenário de referência não encontrado."));
                if (produto.getTipoProduto() != tipoProduto) {
                    throw erro(
                            HttpStatus.BAD_REQUEST,
                            "O produto do cenário precisa pertencer ao tipo " + tipoProduto + ".");
                }
            }
        }
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }

    private String normalizarObrigatorio(String valor, String campo) {
        String normalizado = normalizar(valor);
        if (normalizado == null) {
            throw erro(HttpStatus.BAD_REQUEST, campo + " é obrigatório.");
        }
        return normalizado;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }
}
