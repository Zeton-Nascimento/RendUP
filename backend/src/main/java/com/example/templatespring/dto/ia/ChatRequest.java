package com.example.templatespring.dto.ia;

import com.example.templatespring.entity.enums.PerfilInvestidor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Informe uma mensagem para o assistente.")
    @Size(max = 1000, message = "A mensagem deve ter no máximo 1000 caracteres.")
    private String mensagem;

    private UUID simulacaoIdAtual;

    private PerfilInvestidor perfil;

    private List<UUID> produtoIds;

    @Min(value = 1, message = "O prazo deve estar entre 1 e 600 meses.")
    @Max(value = 600, message = "O prazo deve estar entre 1 e 600 meses.")
    private Integer prazoMesesRefinamento;

    @DecimalMin(value = "0.00", message = "O valor inicial não pode ser negativo.")
    private BigDecimal valorInicialRefinamento;

    @DecimalMin(value = "0.00", message = "O aporte mensal não pode ser negativo.")
    private BigDecimal aporteMensalRefinamento;
}
