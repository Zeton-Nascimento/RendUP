package com.example.templatespring.dto.produto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultadoImportacaoResponse {

    private UUID importacaoId;
    private LocalDateTime dataImportacao;
    private int totalItens;
    private int inseridos;
    private int atualizados;
    private int rejeitados;
    private String versaoCatalogo;
    private String hashImportacao;
    private List<ImportacaoErroResponse> erros;
}
