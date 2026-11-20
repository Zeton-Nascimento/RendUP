package com.example.templatespring.dto.regra;

import com.example.templatespring.entity.enums.EventoFinanceiro;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegraFinanceiraEventoRequest {

    @NotNull(message = "evento é obrigatório.")
    private EventoFinanceiro evento;

    @NotBlank(message = "descricao da regra é obrigatória.")
    @Size(max = 1000, message = "descricao deve ter no máximo 1000 caracteres.")
    private String descricao;

    @NotBlank(message = "formula da regra é obrigatória.")
    @Size(max = 1000, message = "formula deve ter no máximo 1000 caracteres.")
    private String formula;

    @NotEmpty(message = "Cada evento deve ter pelo menos um cenário de referência.")
    @Valid
    private List<CenarioReferenciaRegraRequest> cenariosReferencia;
}
