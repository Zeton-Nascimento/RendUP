package com.example.templatespring.dto.produto.request;

import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;

public record ProdutoFiltroParams(
        TipoProduto tipo,
        String nivelRisco,
        TipoLiquidez liquidez,
        String nome,
        String ordenacao,
        int page,
        int size) {
}
