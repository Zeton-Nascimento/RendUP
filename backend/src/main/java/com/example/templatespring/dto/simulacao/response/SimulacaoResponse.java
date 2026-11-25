package com.example.templatespring.dto.simulacao.response;

import com.example.templatespring.entity.enums.OrigemSimulacao;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.enums.TipoDadoSimulacao;
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
    private PerfilInvestidor perfil;
    private String fonteDados;
    private TipoDadoSimulacao tipoDado;
    private java.time.LocalDateTime dataReferencia;
    private String metodologia;
    private String limitacao;
    private boolean disponivelParaDecisao;
    private String textoEntrada;
    private LocalDateTime createdAt;
}
