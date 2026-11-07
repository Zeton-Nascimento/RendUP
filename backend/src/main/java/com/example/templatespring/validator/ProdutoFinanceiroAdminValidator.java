package com.example.templatespring.validator;

import com.example.templatespring.dto.produto.request.ItemCatalogoProdutoRequest;
import com.example.templatespring.dto.produto.request.ProdutoFinanceiroRequest;
import com.example.templatespring.entity.enums.Indexador;
import com.example.templatespring.entity.enums.TipoLiquidez;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProdutoFinanceiroAdminValidator {

    private static final Set<String> RISCOS_VALIDOS = Set.of("BAIXO", "MEDIO", "ALTO");

    public ProdutoFinanceiroRequest validar(ProdutoFinanceiroRequest request) {
        if (request == null) {
            throw erro("Dados do produto são obrigatórios.");
        }

        request.setNome(validarTexto(request.getNome(), "Nome do produto é obrigatório."));
        request.setDescricao(validarTexto(request.getDescricao(), "Descrição do produto é obrigatória."));
        request.setNivelRisco(validarRisco(request.getNivelRisco()));
        validarCampos(
                request.getTipoProduto(),
                request.getIndexador(),
                request.getPercentualIndexador(),
                request.getTaxaPrefixada(),
                request.getRentabilidadePercentual(),
                request.getPrazoDias(),
                request.getCarenciaDias(),
                request.getLiquidez(),
                request.getDiasLiquidez(),
                request.getValorMinimo());
        if (request.getAtivo() == null) {
            throw erro("A situação ativa do produto é obrigatória.");
        }
        request.setTaxas(normalizar(request.getTaxas()));
        request.setTributacao(normalizar(request.getTributacao()));
        return request;
    }

    public void validarImportacao(ItemCatalogoProdutoRequest item) {
        if (item == null) {
            throw erro("Item de catálogo inválido.");
        }

        item.setNome(validarTexto(item.getNome(), "Nome do produto é obrigatório."));
        item.setDescricao(validarTexto(item.getDescricao(), "Descrição do produto é obrigatória."));
        item.setNivelRisco(validarRisco(item.getNivelRisco()));
        validarCampos(
                item.getTipoProduto(),
                item.getIndexador(),
                item.getPercentualIndexador(),
                item.getTaxaPrefixada(),
                item.getRentabilidadePercentual(),
                item.getPrazoDias(),
                item.getCarenciaDias(),
                item.getLiquidez(),
                item.getDiasLiquidez(),
                item.getValorMinimo());
        item.setTaxas(normalizar(item.getTaxas()));
        item.setTributacao(normalizar(item.getTributacao()));
        item.setCodigoExterno(normalizar(item.getCodigoExterno()));
        item.setOrigemCatalogo(normalizar(item.getOrigemCatalogo()));
        item.setVersaoCatalogo(normalizar(item.getVersaoCatalogo()));
        item.setPayloadOriginal(normalizar(item.getPayloadOriginal()));
    }

    private void validarCampos(
            Object tipoProduto,
            Indexador indexador,
            BigDecimal percentualIndexador,
            BigDecimal taxaPrefixada,
            BigDecimal rentabilidade,
            Integer prazoDias,
            Integer carenciaDias,
            TipoLiquidez liquidez,
            Integer diasLiquidez,
            BigDecimal valorMinimo) {
        if (tipoProduto == null) {
            throw erro("Tipo de produto é obrigatório.");
        }
        if (indexador == null) {
            throw erro("Indexador é obrigatório.");
        }
        if (rentabilidade == null || rentabilidade.compareTo(BigDecimal.ZERO) < 0) {
            throw erro("Rentabilidade deve ser maior ou igual a zero.");
        }
        if (valorMinimo == null || valorMinimo.compareTo(BigDecimal.ZERO) < 0) {
            throw erro("Valor mínimo deve ser maior ou igual a zero.");
        }
        if (prazoDias == null || prazoDias < 0) {
            throw erro("Prazo deve ser maior ou igual a zero.");
        }
        if (carenciaDias != null && (carenciaDias < 0 || carenciaDias > prazoDias)) {
            throw erro("Carência deve estar entre zero e o prazo do produto.");
        }
        if (percentualIndexador != null && percentualIndexador.compareTo(BigDecimal.ZERO) <= 0) {
            throw erro("Percentual do indexador deve ser maior que zero.");
        }
        if (taxaPrefixada != null && taxaPrefixada.compareTo(BigDecimal.ZERO) <= 0) {
            throw erro("Taxa prefixada deve ser maior que zero.");
        }
        if (indexador == Indexador.CDI && percentualIndexador == null) {
            throw erro("Produtos indexados ao CDI exigem percentual do indexador.");
        }
        if (indexador == Indexador.PREFIXADO && taxaPrefixada == null) {
            throw erro("Produtos prefixados exigem taxa prefixada.");
        }
        if (liquidez == null) {
            throw erro("Liquidez é obrigatória.");
        }
        if (diasLiquidez != null && diasLiquidez < 0) {
            throw erro("Dias de liquidez não podem ser negativos.");
        }
        if (liquidez == TipoLiquidez.D_PLUS_N && (diasLiquidez == null || diasLiquidez <= 0)) {
            throw erro("Liquidez D+N exige dias de liquidez maiores que zero.");
        }
        if (liquidez != TipoLiquidez.D_PLUS_N && diasLiquidez != null && diasLiquidez != 0) {
            throw erro("A liquidez escolhida deve usar dias de liquidez iguais a zero.");
        }
    }

    private String validarRisco(String risco) {
        String normalizado = normalizar(risco);
        if (normalizado == null || !RISCOS_VALIDOS.contains(normalizado.toUpperCase(Locale.ROOT))) {
            throw erro("Nível de risco inválido. Use BAIXO, MEDIO ou ALTO.");
        }
        return normalizado.toUpperCase(Locale.ROOT);
    }

    private String validarTexto(String valor, String mensagem) {
        String normalizado = normalizar(valor);
        if (normalizado == null) {
            throw erro(mensagem);
        }
        return normalizado;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
