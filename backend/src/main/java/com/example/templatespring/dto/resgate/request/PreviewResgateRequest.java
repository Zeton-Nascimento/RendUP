package com.example.templatespring.dto.resgate.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PreviewResgateRequest {

    @DecimalMin(value = "0.01", message = "O valor do resgate deve ser maior que zero.")
    private BigDecimal valorResgate;
}
