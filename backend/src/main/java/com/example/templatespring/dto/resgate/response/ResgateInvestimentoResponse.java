package com.example.templatespring.dto.resgate.response;

import com.example.templatespring.entity.enums.StatusInvestimento;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class ResgateInvestimentoResponse {

    private UUID investimentoId;
    private String nomeProduto;
    private TipoProduto tipoProduto;
    private StatusInvestimento status;
    private BigDecimal valorAplicado;
    private BigDecimal valorAtual;
    private BigDecimal valorResgatavel;
    private LocalDate dataAplicacao;
    private LocalDate dataVencimento;
    private Integer carenciaDias;
    private TipoLiquidez liquidez;
    private String nivelRisco;
    private boolean bloqueado;
}
