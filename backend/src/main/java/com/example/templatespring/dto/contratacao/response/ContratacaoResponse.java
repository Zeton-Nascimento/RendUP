package com.example.templatespring.dto.contratacao.response;

import com.example.templatespring.entity.enums.TipoLiquidez;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class ContratacaoResponse {

    private UUID investimentoId;
    private UUID produtoFinanceiroId;
    private String nomeProduto;
    private BigDecimal valorAplicado;
    private BigDecimal saldoDisponivel;
    private BigDecimal valorBrutoEstimado;
    private BigDecimal impostoEstimado;
    private BigDecimal valorLiquidoEstimado;
    private BigDecimal taxaAnualPercentual;
    private String nivelRisco;
    private TipoLiquidez liquidez;
    private Integer carenciaDias;
    private Integer prazoDias;
    private LocalDate dataVencimento;
    private LocalDateTime contratadoEm;
}
