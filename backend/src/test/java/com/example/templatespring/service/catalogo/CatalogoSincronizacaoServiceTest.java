package com.example.templatespring.service.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.templatespring.dto.produto.request.ItemCatalogoProdutoRequest;
import com.example.templatespring.dto.produto.response.CatalogoFonteResponse;
import com.example.templatespring.dto.produto.response.ResultadoImportacaoResponse;
import com.example.templatespring.entity.ImportacaoCatalogo;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.exception.CatalogoFonteException;
import com.example.templatespring.repository.ImportacaoCatalogoRepository;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import com.example.templatespring.validator.ProdutoFinanceiroAdminValidator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CatalogoSincronizacaoServiceTest {

    @Mock
    private CatalogoFonteClient fonteClient;

    @Mock
    private ProdutoFinanceiroRepository produtoRepository;

    @Mock
    private ProdutoFinanceiroAdminValidator validator;

    @Mock
    private ImportacaoCatalogoRepository importacaoRepository;

    private CatalogoSincronizacaoService service;

    @BeforeEach
    void setUp() {
        service = new CatalogoSincronizacaoService(
                fonteClient,
                produtoRepository,
                validator,
                importacaoRepository);
        when(importacaoRepository.save(any(ImportacaoCatalogo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sincronizaCatalogoValidoEAtualizaProdutos() {
        ItemCatalogoProdutoRequest item = itemValido();
        CatalogoFonteResponse fonte = fonte("v1.0.0", List.of(item));

        when(fonteClient.buscarCatalogo()).thenReturn(fonte);
        when(produtoRepository.findByOrigemCatalogoAndCodigoExterno("MOCK_CATALOGO_EXTERNO", "CDB-001"))
                .thenReturn(Optional.empty());
        when(produtoRepository.findByNomeIgnoreCaseAndTipoProduto(item.getNome(), item.getTipoProduto()))
                .thenReturn(Optional.empty());
        when(produtoRepository.findByOrigemCatalogo("MOCK_CATALOGO_EXTERNO"))
                .thenReturn(List.of());

        ResultadoImportacaoResponse resultado = service.sincronizar();

        assertThat(resultado.getStatus()).isEqualTo("SUCESSO");
        assertThat(resultado.getVersaoCatalogo()).isEqualTo("v1.0.0");
        assertThat(resultado.getOrigemCatalogo()).isEqualTo("MOCK_CATALOGO_EXTERNO");
        assertThat(resultado.getInseridos()).isEqualTo(1);
        assertThat(resultado.getRejeitados()).isZero();
        verify(produtoRepository).save(any(ProdutoFinanceiro.class));
        verify(importacaoRepository).save(any(ImportacaoCatalogo.class));
    }

    @Test
    void registraFalhaDaFonteSemAlterarProdutos() {
        when(fonteClient.buscarCatalogo())
                .thenThrow(new CatalogoFonteException("Fonte indisponível."));

        ResultadoImportacaoResponse resultado = service.sincronizar();

        assertThat(resultado.getStatus()).isEqualTo("FALHA");
        assertThat(resultado.getMensagem()).isEqualTo("Fonte indisponível.");
        assertThat(resultado.getRejeitados()).isEqualTo(1);
        verify(produtoRepository, never()).save(any(ProdutoFinanceiro.class));
        verify(importacaoRepository).save(any(ImportacaoCatalogo.class));
    }

    @Test
    void rejeitaPayloadInvalidoEPreservaCatalogoAnterior() {
        ItemCatalogoProdutoRequest item = itemValido();
        CatalogoFonteResponse fonte = fonte("v1.0.0", List.of(item));

        when(fonteClient.buscarCatalogo()).thenReturn(fonte);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do produto é obrigatório."))
                .when(validator).validarImportacao(item);

        ResultadoImportacaoResponse resultado = service.sincronizar();

        assertThat(resultado.getStatus()).isEqualTo("FALHA");
        assertThat(resultado.getRejeitados()).isEqualTo(1);
        assertThat(resultado.getMensagem()).contains("não foi aplicado");
        verify(produtoRepository, never()).save(any(ProdutoFinanceiro.class));
        verify(importacaoRepository).save(any(ImportacaoCatalogo.class));
    }

    private CatalogoFonteResponse fonte(String versao, List<ItemCatalogoProdutoRequest> itens) {
        CatalogoFonteResponse fonte = new CatalogoFonteResponse();
        fonte.setOrigem("MOCK_CATALOGO_EXTERNO");
        fonte.setVersao(versao);
        fonte.setItens(itens);
        return fonte;
    }

    private ItemCatalogoProdutoRequest itemValido() {
        ItemCatalogoProdutoRequest item = new ItemCatalogoProdutoRequest();
        item.setCodigoExterno("CDB-001");
        item.setTipoProduto(com.example.templatespring.entity.enums.TipoProduto.CDB);
        item.setNome("CDB Mock");
        item.setDescricao("Produto mockado para sincronização.");
        item.setIndexador(com.example.templatespring.entity.enums.Indexador.CDI);
        item.setPercentualIndexador(BigDecimal.valueOf(105));
        item.setCarenciaDias(0);
        item.setPrazoDias(365);
        item.setLiquidez(com.example.templatespring.entity.enums.TipoLiquidez.DIARIA);
        item.setDiasLiquidez(0);
        item.setValorMinimo(BigDecimal.valueOf(100));
        item.setNivelRisco("BAIXO");
        item.setRentabilidadePercentual(BigDecimal.valueOf(10.5));
        item.setAtivo(true);
        return item;
    }
}
