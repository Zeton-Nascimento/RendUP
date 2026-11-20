package com.example.templatespring.dto.regra;

import com.example.templatespring.entity.enums.EventoFinanceiro;
import com.example.templatespring.entity.enums.StatusRegraFinanceira;
import com.example.templatespring.entity.enums.TipoProduto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RegraFinanceiraResponse {

    private UUID id;
    private TipoProduto tipoProduto;
    private String versao;
    private StatusRegraFinanceira status;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean prontaParaAplicacao;
    private List<String> pendencias;
    private List<RegraFinanceiraEventoResponse> eventos;

    @Builder
    @Getter
    public static class RegraFinanceiraEventoResponse {
        private EventoFinanceiro evento;
        private String descricao;
        private String formula;
        private List<CenarioReferenciaResponse> cenariosReferencia;
    }

    @Builder
    @Getter
    public static class CenarioReferenciaResponse {
        private UUID id;
        private UUID produtoId;
        private String produtoNome;
        private String nome;
        private BigDecimal valorEntrada;
        private BigDecimal aporteMensal;
        private Integer prazoDias;
        private BigDecimal taxaPercentual;
        private BigDecimal valorBrutoEsperado;
        private BigDecimal impostoEsperado;
        private BigDecimal valorLiquidoEsperado;
        private BigDecimal valorArredondadoEsperado;
        private Integer casasDecimais;
        private String observacoes;
    }
}
