package com.example.templatespring.dto.simulacao.request;

import com.example.templatespring.entity.enums.PerfilInvestidor;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SimulacaoRequest {

    @NotNull
    private BigDecimal valorInicial;

    @NotNull
    private BigDecimal aporteMensal;

    private int prazoMeses;

    @NotNull
    private List<UUID> produtoIds;

    @NotNull
    private PerfilInvestidor perfil;
}
