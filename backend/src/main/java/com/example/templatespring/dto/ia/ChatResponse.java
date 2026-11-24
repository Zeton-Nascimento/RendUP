package com.example.templatespring.dto.ia;

import com.example.templatespring.dto.simulacao.response.SimulacaoResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatResponse {

    private String resposta;
    private IntencaoEnum intencao;
    private SimulacaoResponse simulacaoAtualizada;
    private boolean fallback;

    @Builder.Default
    private List<String> sugestoes = List.of();

    public String getDisclaimer() {
        return "As respostas são informativas e não constituem recomendação ou decisão de investimento.";
    }
}
