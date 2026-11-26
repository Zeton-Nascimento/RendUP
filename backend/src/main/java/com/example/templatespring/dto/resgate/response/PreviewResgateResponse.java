package com.example.templatespring.dto.resgate.response;

import com.example.templatespring.entity.enums.StatusInvestimento;
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
public class PreviewResgateResponse {

    private boolean validoParaConfirmacao;
    private List<String> erros;
    private UUID investimentoId;
    private String nomeProduto;
    private StatusInvestimento status;
    private BigDecimal valorAtual;
    private BigDecimal valorResgatavel;
    private BigDecimal valorBruto;
    private BigDecimal impostoEstimado;
    private BigDecimal descontoAntecipacao;
    private BigDecimal valorLiquido;
    private BigDecimal saldoRemanescente;
    private boolean antecipado;
    private LocalDate dataAplicacao;
    private LocalDate dataVencimento;
    private Integer carenciaDias;
    private TipoLiquidez liquidez;
    private String nivelRisco;
    private boolean bloqueado;
}
