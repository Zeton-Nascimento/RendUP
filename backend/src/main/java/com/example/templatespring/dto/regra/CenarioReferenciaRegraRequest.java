package com.example.templatespring.dto.regra;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CenarioReferenciaRegraRequest {

    private UUID produtoId;

    @NotBlank(message = "nome do cenário é obrigatório.")
    @Size(max = 120, message = "nome do cenário deve ter no máximo 120 caracteres.")
    private String nome;

    @DecimalMin(value = "0.00", message = "valorEntrada deve ser maior ou igual a zero.")
    private BigDecimal valorEntrada;

    @DecimalMin(value = "0.00", message = "aporteMensal deve ser maior ou igual a zero.")
    private BigDecimal aporteMensal;

    @Min(value = 0, message = "prazoDias deve ser maior ou igual a zero.")
    @Max(value = 36500, message = "prazoDias deve ser no máximo 36500.")
    private Integer prazoDias;

    @DecimalMin(value = "0.00", message = "taxaPercentual deve ser maior ou igual a zero.")
    private BigDecimal taxaPercentual;

    @DecimalMin(value = "0.00", message = "valorBrutoEsperado deve ser maior ou igual a zero.")
    private BigDecimal valorBrutoEsperado;

    @DecimalMin(value = "0.00", message = "impostoEsperado deve ser maior ou igual a zero.")
    private BigDecimal impostoEsperado;

    @DecimalMin(value = "0.00", message = "valorLiquidoEsperado deve ser maior ou igual a zero.")
    private BigDecimal valorLiquidoEsperado;

    @DecimalMin(value = "0.00", message = "valorArredondadoEsperado deve ser maior ou igual a zero.")
    private BigDecimal valorArredondadoEsperado;

    @Min(value = 0, message = "casasDecimais deve ser maior ou igual a zero.")
    @Max(value = 6, message = "casasDecimais deve ser no máximo 6.")
    private Integer casasDecimais;

    @Size(max = 500, message = "observacoes deve ter no máximo 500 caracteres.")
    private String observacoes;
}
