package com.example.templatespring.dto.contratacao.response;

import com.example.templatespring.entity.enums.TipoLiquidez;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PreviewContratacaoResponse {

    private boolean validoParaConfirmacao;
    private List<String> erros;
    private UUID produtoFinanceiroId;
    private String nomeProduto;
    private BigDecimal valor;
    private BigDecimal valorMinimo;
    private BigDecimal saldoDisponivel;
    private BigDecimal taxaAnualPercentual;
    private BigDecimal valorBrutoEstimado;
    private BigDecimal impostoEstimado;
    private BigDecimal valorLiquidoEstimado;
    private String taxas;
    private String tributacao;
    private String nivelRisco;
    private TipoLiquidez liquidez;
    private Integer carenciaDias;
    private Integer prazoDias;
    private LocalDate dataVencimento;
}
