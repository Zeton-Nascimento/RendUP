package com.example.templatespring.dto.simulacao.response;

import com.example.templatespring.entity.enums.OrigemSimulacao;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulacaoResumoResponse {

    private UUID id;
    private LocalDateTime createdAt;
    private OrigemSimulacao origem;
    private PerfilInvestidor perfil;
    private BigDecimal valorInicial;
    private BigDecimal aporteMensal;
    private Integer prazoMeses;
    private List<SimulacaoResumoCenarioResponse> cenarios;
    private String textoCompartilhavel;
    private String aviso;
}
