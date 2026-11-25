package com.example.templatespring.dto.simulacao.response;

import com.example.templatespring.entity.enums.OrigemSimulacao;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulacaoHistoricoResponse {

    private UUID id;
    private LocalDateTime createdAt;
    private OrigemSimulacao origem;
    private PerfilInvestidor perfil;
    private BigDecimal valorInicial;
    private BigDecimal aporteMensal;
    private Integer prazoMeses;
    private Integer quantidadeCenarios;
    private String melhorProduto;
    private BigDecimal melhorValorFinalLiquido;
}
