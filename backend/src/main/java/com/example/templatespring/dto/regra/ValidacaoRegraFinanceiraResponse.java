package com.example.templatespring.dto.regra;

import com.example.templatespring.entity.enums.TipoProduto;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ValidacaoRegraFinanceiraResponse {

    private TipoProduto tipoProduto;
    private String statusDocumento;
    private boolean podeProcessar;
    private List<String> pendencias;
}
