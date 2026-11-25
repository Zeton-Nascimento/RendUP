package com.example.templatespring.dto.simulacao.response;

import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulacaoResumoCenarioResponse {

    private String produto;
    private TipoProduto tipoProduto;
    private BigDecimal valorFinalLiquido;
    private BigDecimal rentabilidade;
    private TipoLiquidez liquidez;
    private String nivelRisco;
}
