package com.example.templatespring.dto.deposito.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepositoRequest {

    @NotNull(message = "Valor do depósito é obrigatório.")
    @DecimalMin(value = "0.01", message = "Valor do depósito deve ser maior que zero.")
    private BigDecimal valor;
}
