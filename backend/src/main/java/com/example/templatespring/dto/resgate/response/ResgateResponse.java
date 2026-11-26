package com.example.templatespring.dto.resgate.response;

import java.math.BigDecimal;
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
public class ResgateResponse {

    private UUID investimentoId;
    private BigDecimal valorBruto;
    private BigDecimal imposto;
    private BigDecimal descontoAntecipacao;
    private BigDecimal valorLiquido;
    private BigDecimal saldoRemanescente;
    private BigDecimal saldoDisponivel;
    private boolean antecipado;
    private LocalDateTime resgatadoEm;
}
