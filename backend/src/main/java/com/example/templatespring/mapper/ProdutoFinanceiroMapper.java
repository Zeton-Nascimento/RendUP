package com.example.templatespring.mapper;

import com.example.templatespring.dto.produto.request.ItemCatalogoProdutoRequest;
import com.example.templatespring.dto.produto.request.ProdutoFinanceiroRequest;
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
                .codigoExterno(produto.getCodigoExterno())
                .origemCatalogo(produto.getOrigemCatalogo())
                .versaoCatalogo(produto.getVersaoCatalogo())
                .payloadOriginal(produto.getPayloadOriginal())
                .codigoExterno(produto.getCodigoExterno())
                .origemCatalogo(produto.getOrigemCatalogo())
                .versaoCatalogo(produto.getVersaoCatalogo())
                .payloadOriginal(produto.getPayloadOriginal())
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

    public static ProdutoFinanceiro toEntity(ProdutoFinanceiroRequest request) {
        ProdutoFinanceiro entity = new ProdutoFinanceiro();
        atualizarEntity(entity, request);
        return entity;
    }

    public static void atualizarEntity(ProdutoFinanceiro entity, ProdutoFinanceiroRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setTipoProduto(request.getTipoProduto());
        entity.setNome(request.getNome());
        entity.setDescricao(request.getDescricao());
        entity.setIndexador(request.getIndexador());
        entity.setPercentualIndexador(request.getPercentualIndexador());
        entity.setTaxaPrefixada(request.getTaxaPrefixada());
        entity.setCarenciaDias(request.getCarenciaDias());
        entity.setPrazoDias(request.getPrazoDias());
        entity.setLiquidez(request.getLiquidez());
        entity.setDiasLiquidez(request.getDiasLiquidez());
        entity.setValorMinimo(request.getValorMinimo());
        entity.setNivelRisco(request.getNivelRisco());
        entity.setRentabilidadePercentual(request.getRentabilidadePercentual());
        entity.setTaxas(request.getTaxas());
        entity.setTributacao(request.getTributacao());
        entity.setAtivo(Boolean.TRUE.equals(request.getAtivo()));
    }

    public static void atualizarEntity(ProdutoFinanceiro entity, ItemCatalogoProdutoRequest item) {
        if (entity == null || item == null) {
            return;
        }

        entity.setTipoProduto(item.getTipoProduto());
        entity.setNome(item.getNome());
        entity.setDescricao(item.getDescricao());
        entity.setIndexador(item.getIndexador());
        entity.setPercentualIndexador(item.getPercentualIndexador());
        entity.setTaxaPrefixada(item.getTaxaPrefixada());
        entity.setCarenciaDias(item.getCarenciaDias());
        entity.setPrazoDias(item.getPrazoDias());
        entity.setLiquidez(item.getLiquidez());
        entity.setDiasLiquidez(item.getDiasLiquidez());
        entity.setValorMinimo(item.getValorMinimo());
        entity.setNivelRisco(item.getNivelRisco());
        entity.setRentabilidadePercentual(item.getRentabilidadePercentual());
        entity.setTaxas(item.getTaxas());
        entity.setTributacao(item.getTributacao());
        entity.setAtivo(item.isAtivo());
        entity.setCodigoExterno(item.getCodigoExterno());
        entity.setOrigemCatalogo(item.getOrigemCatalogo());
        entity.setVersaoCatalogo(item.getVersaoCatalogo());
        entity.setPayloadOriginal(item.getPayloadOriginal());
    }
}
