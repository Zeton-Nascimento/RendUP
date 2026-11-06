package com.example.templatespring.dto.produto.response;

import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import com.example.templatespring.entity.enums.Indexador;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProdutoFinanceiroResponse {

    private UUID id;
    private TipoProduto tipoProduto;
    private String nome;
    private String descricao;
    private Indexador indexador;
    private BigDecimal percentualIndexador;
    private BigDecimal taxaPrefixada;
    private Integer carenciaDias;
    private String taxas;
    private String tributacao;
    private BigDecimal rentabilidadePercentual;
    private BigDecimal valorMinimo;
    private String nivelRisco;
    private TipoLiquidez liquidez;
    private Integer diasLiquidez;
    private Integer prazoDias;
    private boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
