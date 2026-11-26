package com.example.templatespring.dto.contratacao.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContratacaoRequest {

    @NotNull(message = "Produto financeiro é obrigatório.")
    private UUID produtoFinanceiroId;

    @NotNull(message = "Valor da contratação é obrigatório.")
    @DecimalMin(value = "0.01", message = "Valor da contratação deve ser maior que zero.")
    private BigDecimal valor;

    @NotNull(message = "A confirmação da contratação é obrigatória.")
    private Boolean confirmar;
}
