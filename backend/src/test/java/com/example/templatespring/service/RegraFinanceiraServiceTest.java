package com.example.templatespring.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.templatespring.dto.regra.CenarioReferenciaRegraRequest;
import com.example.templatespring.dto.regra.RegraFinanceiraEventoRequest;
import com.example.templatespring.dto.regra.RegraFinanceiraRequest;
import com.example.templatespring.dto.regra.RegraFinanceiraResponse;
import com.example.templatespring.dto.regra.ValidacaoRegraFinanceiraResponse;
import com.example.templatespring.entity.enums.EventoFinanceiro;
import com.example.templatespring.entity.enums.StatusRegraFinanceira;
import com.example.templatespring.entity.enums.TipoProduto;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import com.example.templatespring.repository.RegraFinanceiraRepository;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RegraFinanceiraServiceTest {

    @Mock
    private RegraFinanceiraRepository repository;

    @Mock
    private ProdutoFinanceiroRepository produtoRepository;

    @Test
    void deveMarcarRegraAprovadaCompletaComoProntaParaAplicacao() {
        RegraFinanceiraService service = new RegraFinanceiraService(repository, produtoRepository);
        ProdutoFinanceiro produto = produtoCdb();
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        when(repository.findByTipoProduto(TipoProduto.CDB)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegraFinanceiraResponse response = service.salvar(
                TipoProduto.CDB,
                request(StatusRegraFinanceira.APROVADA, Arrays.asList(EventoFinanceiro.values()), produto.getId()));

        assertTrue(response.isProntaParaAplicacao());
        assertTrue(response.getPendencias().isEmpty());
    }

    @Test
    void naoDeveAprovarDocumentoComEventoAusente() {
        RegraFinanceiraService service = new RegraFinanceiraService(repository, produtoRepository);

        assertThrows(ResponseStatusException.class, () -> service.salvar(
                TipoProduto.CDB,
                request(StatusRegraFinanceira.APROVADA, List.of(EventoFinanceiro.APORTE))));
    }

    @Test
    void deveBloquearValidacaoQuandoRegraNaoFoiDocumentada() {
        RegraFinanceiraService service = new RegraFinanceiraService(repository, produtoRepository);
        when(repository.findByTipoProduto(TipoProduto.CDB)).thenReturn(Optional.empty());

        ValidacaoRegraFinanceiraResponse response = service.validar(TipoProduto.CDB);

        assertFalse(response.isPodeProcessar());
        assertTrue("AUSENTE".equals(response.getStatusDocumento()));
        assertFalse(response.getPendencias().isEmpty());
    }

    @Test
    void deveBloquearGuardQuandoDocumentoEstaAusente() {
        RegraFinanceiraService service = new RegraFinanceiraService(repository, produtoRepository);
        when(repository.findByTipoProduto(TipoProduto.CDB)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.exigirRegraAprovada(TipoProduto.CDB));
    }

    private RegraFinanceiraRequest request(StatusRegraFinanceira status, List<EventoFinanceiro> eventos) {
        return request(status, eventos, UUID.randomUUID());
    }

    private RegraFinanceiraRequest request(
            StatusRegraFinanceira status,
            List<EventoFinanceiro> eventos,
            UUID produtoId) {
        RegraFinanceiraRequest request = new RegraFinanceiraRequest();
        request.setTipoProduto(TipoProduto.CDB);
        request.setVersao("2026.1");
        request.setStatus(status);
        request.setEventos(eventos.stream().map(tipo -> evento(tipo, produtoId)).toList());
        return request;
    }

    private RegraFinanceiraEventoRequest evento(EventoFinanceiro tipo, UUID produtoId) {
        RegraFinanceiraEventoRequest evento = new RegraFinanceiraEventoRequest();
        evento.setEvento(tipo);
        evento.setDescricao("Regra documentada para " + tipo);
        evento.setFormula("Aplicar a regra aprovada do produto");

        CenarioReferenciaRegraRequest cenario = new CenarioReferenciaRegraRequest();
        cenario.setProdutoId(produtoId);
        cenario.setNome("Cenário padrão");
        cenario.setValorEntrada(java.math.BigDecimal.valueOf(100));
        cenario.setPrazoDias(30);
        evento.setCenariosReferencia(List.of(cenario));
        return evento;
    }

    private ProdutoFinanceiro produtoCdb() {
        ProdutoFinanceiro produto = new ProdutoFinanceiro();
        produto.setId(UUID.randomUUID());
        produto.setTipoProduto(TipoProduto.CDB);
        produto.setNome("CDB de referência");
        return produto;
    }
}
