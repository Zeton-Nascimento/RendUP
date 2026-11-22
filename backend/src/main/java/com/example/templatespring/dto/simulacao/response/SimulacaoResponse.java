package com.example.templatespring.dto.simulacao.response;

import com.example.templatespring.entity.enums.OrigemSimulacao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulacaoResponse {

    private UUID id;
    private List<CenarioResponse> cenarios;
    private OrigemSimulacao origem;
    private String textoEntrada;
    private LocalDateTime createdAt;
}
