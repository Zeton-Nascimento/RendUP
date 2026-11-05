package com.example.templatespring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ProdutoFinanceiroRepositoryTest {

    @Autowired
    private ProdutoFinanceiroRepository repository;

    @Test
    void listaSomenteProdutosAtivosEAplicaFiltrosDoCatalogo() {
        repository.save(produto("CDB Diário", TipoProduto.CDB, "BAIXO", true));
        repository.save(produto("Fundo Balanceado", TipoProduto.FUNDO, "MEDIO", true));
        repository.save(produto("CDB Inativo", TipoProduto.CDB, "BAIXO", false));

        Page<ProdutoFinanceiro> resultado = repository.buscarAtivosFiltrados(
                TipoProduto.CDB,
                "BAIXO",
                TipoLiquidez.DIARIA,
                "diário",
                PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).extracting(ProdutoFinanceiro::getNome).containsExactly("CDB Diário");
    }

    private ProdutoFinanceiro produto(String nome, TipoProduto tipo, String risco, boolean ativo) {
        ProdutoFinanceiro produto = new ProdutoFinanceiro();
        produto.setTipoProduto(tipo);
        produto.setNome(nome);
        produto.setDescricao("Produto de teste");
        produto.setRentabilidadePercentual(BigDecimal.TEN);
        produto.setValorMinimo(BigDecimal.valueOf(100));
        produto.setNivelRisco(risco);
        produto.setLiquidez(TipoLiquidez.DIARIA);
        produto.setDiasLiquidez(0);
        produto.setPrazoDias(365);
        produto.setAtivo(ativo);
        produto.setCreatedAt(LocalDateTime.now());
        produto.setUpdatedAt(LocalDateTime.now());
        return produto;
    }
}
