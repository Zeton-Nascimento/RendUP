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
    private static final int NOME_MINIMO = 2;
    private static final int NOME_MAXIMO = 120;
    private static final int DESCRICAO_MAXIMA = 500;
    private static final int TEXTO_ADMIN_MAXIMO = 250;
    private static final int DIAS_MAXIMO = 36_500;
    private static final BigDecimal PERCENTUAL_MAXIMO = BigDecimal.valueOf(1_000);
    private static final BigDecimal VALOR_MINIMO_MAXIMO = BigDecimal.valueOf(1_000_000_000L);

    public ProdutoFinanceiroRequest validar(ProdutoFinanceiroRequest request) {
        if (request == null) {
            throw erro("Dados do produto são obrigatórios.");
        }

        request.setNome(validarTexto(request.getNome(), "Nome do produto é obrigatório.", NOME_MINIMO, NOME_MAXIMO));
        request.setDescricao(validarTexto(request.getDescricao(), "Descrição do produto é obrigatória.", 1, DESCRICAO_MAXIMA));
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
        request.setTaxas(validarTextoOpcional(request.getTaxas(), "Taxas", TEXTO_ADMIN_MAXIMO));
        request.setTributacao(validarTextoOpcional(request.getTributacao(), "Tributação", TEXTO_ADMIN_MAXIMO));
        return request;
    }

    public void validarImportacao(ItemCatalogoProdutoRequest item) {
        if (item == null) {
            throw erro("Item de catálogo inválido.");
        }

        item.setNome(validarTexto(item.getNome(), "Nome do produto é obrigatório.", NOME_MINIMO, NOME_MAXIMO));
        item.setDescricao(validarTexto(item.getDescricao(), "Descrição do produto é obrigatória.", 1, DESCRICAO_MAXIMA));
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
        item.setTaxas(validarTextoOpcional(item.getTaxas(), "Taxas", TEXTO_ADMIN_MAXIMO));
        item.setTributacao(validarTextoOpcional(item.getTributacao(), "Tributação", TEXTO_ADMIN_MAXIMO));
        item.setCodigoExterno(validarTextoOpcional(item.getCodigoExterno(), "Código externo", 100));
        item.setOrigemCatalogo(validarTextoOpcional(item.getOrigemCatalogo(), "Origem do catálogo", 100));
        item.setVersaoCatalogo(validarTextoOpcional(item.getVersaoCatalogo(), "Versão do catálogo", 100));
        item.setPayloadOriginal(validarTextoOpcional(item.getPayloadOriginal(), "Payload original", 10_000));
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
        if (rentabilidade.compareTo(PERCENTUAL_MAXIMO) > 0) {
            throw erro("Rentabilidade deve ser no máximo 1000%.");
        }
        if (valorMinimo == null || valorMinimo.compareTo(BigDecimal.ZERO) < 0) {
            throw erro("Valor mínimo deve ser maior ou igual a zero.");
        }
        if (valorMinimo.compareTo(VALOR_MINIMO_MAXIMO) > 0) {
            throw erro("Valor mínimo deve ser no máximo R$ 1.000.000.000,00.");
        }
        if (prazoDias == null || prazoDias < 0) {
            throw erro("Prazo deve ser maior ou igual a zero.");
        }
        if (prazoDias > DIAS_MAXIMO) {
            throw erro("Prazo deve ser no máximo 36500 dias.");
        }
        if (carenciaDias != null && (carenciaDias < 0 || carenciaDias > prazoDias)) {
            throw erro("Carência deve estar entre zero e o prazo do produto.");
        }
        if (carenciaDias != null && carenciaDias > DIAS_MAXIMO) {
            throw erro("Carência deve ser no máximo 36500 dias.");
        }
        if (percentualIndexador != null && percentualIndexador.compareTo(BigDecimal.ZERO) <= 0) {
            throw erro("Percentual do indexador deve ser maior que zero.");
        }
        if (percentualIndexador != null && percentualIndexador.compareTo(PERCENTUAL_MAXIMO) > 0) {
            throw erro("Percentual do indexador deve ser no máximo 1000.");
        }
        if (taxaPrefixada != null && taxaPrefixada.compareTo(BigDecimal.ZERO) <= 0) {
            throw erro("Taxa prefixada deve ser maior que zero.");
        }
        if (taxaPrefixada != null && taxaPrefixada.compareTo(PERCENTUAL_MAXIMO) > 0) {
            throw erro("Taxa prefixada deve ser no máximo 1000.");
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
        if (diasLiquidez != null && diasLiquidez > DIAS_MAXIMO) {
            throw erro("Dias de liquidez devem ser no máximo 36500.");
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

    private String validarTexto(String valor, String mensagem, int minimo, int maximo) {
        String normalizado = normalizar(valor);
        if (normalizado == null || normalizado.length() < minimo) {
            throw erro(mensagem);
        }
        if (normalizado.length() > maximo) {
            throw erro("O texto informado deve ter no máximo " + maximo + " caracteres.");
        }
        return normalizado;
    }

    private String validarTextoOpcional(String valor, String campo, int maximo) {
        String normalizado = normalizar(valor);
        if (normalizado != null && normalizado.length() > maximo) {
            throw erro(campo + " deve ter no máximo " + maximo + " caracteres.");
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
