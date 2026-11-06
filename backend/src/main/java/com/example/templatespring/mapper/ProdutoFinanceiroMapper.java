package com.example.templatespring.mapper;

import com.example.templatespring.dto.produto.response.ProdutoFinanceiroResponse;
import com.example.templatespring.entity.ProdutoFinanceiro;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProdutoFinanceiroMapper {

    public static ProdutoFinanceiroResponse toResponse(ProdutoFinanceiro produto) {
        return ProdutoFinanceiroResponse.builder()
                .id(produto.getId())
                .tipoProduto(produto.getTipoProduto())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .indexador(produto.getIndexador())
                .percentualIndexador(produto.getPercentualIndexador())
                .taxaPrefixada(produto.getTaxaPrefixada())
                .carenciaDias(produto.getCarenciaDias())
                .taxas(produto.getTaxas())
                .tributacao(produto.getTributacao())
                .rentabilidadePercentual(produto.getRentabilidadePercentual())
                .valorMinimo(produto.getValorMinimo())
                .nivelRisco(produto.getNivelRisco())
                .liquidez(produto.getLiquidez())
                .diasLiquidez(produto.getDiasLiquidez())
                .prazoDias(produto.getPrazoDias())
                .ativo(produto.isAtivo())
                .createdAt(produto.getCreatedAt())
                .updatedAt(produto.getUpdatedAt())
                .build();
    }
}
