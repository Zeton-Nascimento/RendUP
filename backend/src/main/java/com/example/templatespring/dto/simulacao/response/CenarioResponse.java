package com.example.templatespring.dto.simulacao.response;

import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CenarioResponse {

    private UUID produtoId;
    private String nomeProduto;
    private TipoProduto tipoProduto;
    private BigDecimal valorFinalBruto;
    private BigDecimal valorFinalLiquido;
    private BigDecimal rentabilidade;
    private TipoLiquidez liquidez;
    private String nivelRisco;
    private List<BigDecimal> evolucaoMensal;
    private Integer ranking;
    private String destaque;
    private BigDecimal valorInicial;
    private BigDecimal aporteMensal;
    private Integer prazoMeses;
}
