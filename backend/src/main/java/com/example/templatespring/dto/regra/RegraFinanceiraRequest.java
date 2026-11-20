package com.example.templatespring.dto.regra;

import com.example.templatespring.entity.enums.StatusRegraFinanceira;
import com.example.templatespring.entity.enums.TipoProduto;
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
public class RegraFinanceiraRequest {

    @NotNull(message = "tipoProduto é obrigatório.")
    private TipoProduto tipoProduto;

    @NotBlank(message = "versao é obrigatória.")
    @Size(max = 40, message = "versao deve ter no máximo 40 caracteres.")
    private String versao;

    @NotNull(message = "status é obrigatório.")
    private StatusRegraFinanceira status = StatusRegraFinanceira.PENDENTE;

    @Size(max = 500, message = "observacoes deve ter no máximo 500 caracteres.")
    private String observacoes;

    @NotEmpty(message = "Informe as regras dos eventos financeiros.")
    @Valid
    private List<RegraFinanceiraEventoRequest> eventos;
}
