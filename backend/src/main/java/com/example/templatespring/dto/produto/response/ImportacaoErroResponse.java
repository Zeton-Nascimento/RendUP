package com.example.templatespring.dto.produto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImportacaoErroResponse {

    private int linha;
    private String motivo;
}
