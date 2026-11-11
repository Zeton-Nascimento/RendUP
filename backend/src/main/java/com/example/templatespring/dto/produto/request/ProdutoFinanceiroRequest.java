package com.example.templatespring.dto.produto.request;

import com.example.templatespring.entity.enums.Indexador;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProdutoFinanceiroRequest {

    @NotNull
    @JsonAlias("tipo_produto")
    private TipoProduto tipoProduto;

    @NotBlank
    @Size(min = 2, max = 120, message = "nome deve ter entre 2 e 120 caracteres")
    private String nome;

    @NotBlank
    @Size(max = 500, message = "descricao deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull
    private Indexador indexador;

    @JsonAlias("percentual_indexador")
    @DecimalMax(value = "1000.00", message = "percentualIndexador deve ser no máximo 1000")
    private BigDecimal percentualIndexador;

    @JsonAlias("taxa_prefixada")
    @DecimalMax(value = "1000.00", message = "taxaPrefixada deve ser no máximo 1000")
    private BigDecimal taxaPrefixada;

    @PositiveOrZero
    @jakarta.validation.constraints.Max(value = 36500, message = "carenciaDias deve ser no máximo 36500")
    @JsonAlias("carencia_dias")
    private Integer carenciaDias;

    @NotNull
    @PositiveOrZero
    @jakarta.validation.constraints.Max(value = 36500, message = "prazoDias deve ser no máximo 36500")
    @JsonAlias("prazo_dias")
    private Integer prazoDias;

    @NotNull
    private TipoLiquidez liquidez;

    @PositiveOrZero
    @jakarta.validation.constraints.Max(value = 36500, message = "diasLiquidez deve ser no máximo 36500")
    @JsonAlias("dias_liquidez")
    private Integer diasLiquidez;

    @NotNull
    @PositiveOrZero
    @DecimalMax(value = "1000000000.00", message = "valorMinimo deve ser no máximo 1000000000")
    @JsonAlias("valor_minimo")
    private BigDecimal valorMinimo;

    @NotBlank
    @JsonAlias("nivel_risco")
    private String nivelRisco;

    @NotNull
    @PositiveOrZero
    @DecimalMax(value = "1000.00", message = "rentabilidadePercentual deve ser no máximo 1000")
    @JsonAlias("rentabilidade_percentual")
    private BigDecimal rentabilidadePercentual;

    @Size(max = 250, message = "taxas deve ter no máximo 250 caracteres")
    private String taxas;
    @Size(max = 250, message = "tributacao deve ter no máximo 250 caracteres")
    private String tributacao;

    @NotNull
    private Boolean ativo = true;
}
