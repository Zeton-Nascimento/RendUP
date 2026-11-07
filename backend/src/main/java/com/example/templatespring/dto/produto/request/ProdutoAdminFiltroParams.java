package com.example.templatespring.dto.produto.request;

import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;

public record ProdutoAdminFiltroParams(
        TipoProduto tipo,
        String nivelRisco,
        TipoLiquidez liquidez,
        String nome,
        Boolean ativo,
        int page,
        int size) {
}
