package com.example.templatespring.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.templatespring.dto.produto.request.ProdutoFinanceiroRequest;
import com.example.templatespring.entity.enums.Indexador;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ProdutoFinanceiroAdminValidatorTest {

    private final ProdutoFinanceiroAdminValidator validator = new ProdutoFinanceiroAdminValidator();

    @Test
    void aceitaProdutoAdministrativoValido() {
        ProdutoFinanceiroRequest request = requestValido();

        validator.validar(request);
    }

    @Test
    void rejeitaProdutoCdiSemPercentualDoIndexador() {
        ProdutoFinanceiroRequest request = requestValido();
        request.setPercentualIndexador(null);

        assertThatThrownBy(() -> validator.validar(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("percentual do indexador");
    }

    @Test
    void rejeitaProdutoComTextoAcimaDoLimite() {
        ProdutoFinanceiroRequest request = requestValido();
        request.setNome("x".repeat(121));

        assertThatThrownBy(() -> validator.validar(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no máximo 120");
    }

    @Test
    void rejeitaProdutoComValorForaDoLimite() {
        ProdutoFinanceiroRequest request = requestValido();
        request.setRentabilidadePercentual(BigDecimal.valueOf(1000.01));

        assertThatThrownBy(() -> validator.validar(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no máximo 1000");
    }

    private ProdutoFinanceiroRequest requestValido() {
        ProdutoFinanceiroRequest request = new ProdutoFinanceiroRequest();
        request.setTipoProduto(TipoProduto.CDB);
        request.setNome("CDB de teste");
        request.setDescricao("Produto usado no teste de administração.");
        request.setIndexador(Indexador.CDI);
        request.setPercentualIndexador(BigDecimal.valueOf(110));
        request.setRentabilidadePercentual(BigDecimal.valueOf(12));
        request.setPrazoDias(365);
        request.setCarenciaDias(30);
        request.setLiquidez(TipoLiquidez.DIARIA);
        request.setDiasLiquidez(0);
        request.setValorMinimo(BigDecimal.valueOf(100));
        request.setNivelRisco("BAIXO");
        request.setAtivo(true);
        return request;
    }
}
