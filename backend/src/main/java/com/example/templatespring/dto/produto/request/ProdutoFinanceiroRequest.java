package com.example.templatespring.dto.produto.request;

import com.example.templatespring.entity.enums.Indexador;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
    private String nome;

    @NotBlank
    private String descricao;

    @NotNull
    private Indexador indexador;

    @JsonAlias("percentual_indexador")
    private BigDecimal percentualIndexador;

    @JsonAlias("taxa_prefixada")
    private BigDecimal taxaPrefixada;

    @PositiveOrZero
    @JsonAlias("carencia_dias")
    private Integer carenciaDias;

    @NotNull
    @PositiveOrZero
    @JsonAlias("prazo_dias")
    private Integer prazoDias;

    @NotNull
    private TipoLiquidez liquidez;

    @PositiveOrZero
    @JsonAlias("dias_liquidez")
    private Integer diasLiquidez;

    @NotNull
    @PositiveOrZero
    @JsonAlias("valor_minimo")
    private BigDecimal valorMinimo;

    @NotBlank
    @JsonAlias("nivel_risco")
    private String nivelRisco;

    @NotNull
    @PositiveOrZero
    @JsonAlias("rentabilidade_percentual")
    private BigDecimal rentabilidadePercentual;

    private String taxas;
    private String tributacao;

    @NotNull
    private Boolean ativo = true;
}
