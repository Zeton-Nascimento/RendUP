package com.example.templatespring.dto.resgate.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResgateRequest {

    @DecimalMin(value = "0.01", message = "O valor do resgate deve ser maior que zero.")
    private BigDecimal valorResgate;

    @NotNull(message = "A confirmação do resgate é obrigatória.")
    private Boolean confirmar;
}
