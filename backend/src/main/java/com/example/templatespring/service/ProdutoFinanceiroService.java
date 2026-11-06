package com.example.templatespring.service;

import com.example.templatespring.dto.produto.request.ProdutoFiltroParams;
import com.example.templatespring.dto.produto.response.ProdutoFinanceiroResponse;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.mapper.ProdutoFinanceiroMapper;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import java.util.List;
import java.util.UUID;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProdutoFinanceiroService {

    private static final Set<String> RISCOS_VALIDOS = Set.of("BAIXO", "MEDIO", "ALTO");
    private static final Set<String> ORDENACOES_VALIDAS = Set.of(
            "NOME", "RENTABILIDADE", "MENOR_VALOR_MINIMO", "MAIOR_LIQUIDEZ", "MENOR_RISCO");

    private final ProdutoFinanceiroRepository repository;

    public ProdutoFinanceiroService(ProdutoFinanceiroRepository repository) {
        this.repository = repository;
    }

    public Page<ProdutoFinanceiroResponse> listarAtivos(ProdutoFiltroParams filtro) {
        String risco = normalizarRisco(filtro.nivelRisco());
        String ordenacao = normalizarOrdenacao(filtro.ordenacao());
        validarPaginacao(filtro.page(), filtro.size());

        Pageable pageable = PageRequest.of(filtro.page(), filtro.size(), montarOrdenacao(ordenacao));
        String nome = normalizarTexto(filtro.nome());

        return repository.buscarAtivosFiltrados(
                        filtro.tipo(), risco, filtro.liquidez(), nome, pageable)
                .map(ProdutoFinanceiroMapper::toResponse);
    }

    public ProdutoFinanceiroResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .filter(ProdutoFinanceiro::isAtivo)
                .map(ProdutoFinanceiroMapper::toResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto financeiro não encontrado."));
    }

    public List<ProdutoFinanceiroResponse> buscarRelacionados(UUID id) {
        ProdutoFinanceiro produto = repository.findById(id)
                .filter(ProdutoFinanceiro::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto financeiro não encontrado."));

        return repository.findByTipoProdutoAndAtivoTrueAndIdNot(
                        produto.getTipoProduto(), id, PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "nome")))
                .stream()
                .map(ProdutoFinanceiroMapper::toResponse)
                .toList();
    }

    private Sort montarOrdenacao(String ordenacao) {
        Sort porNome = Sort.by(Sort.Direction.ASC, "nome");

        return switch (ordenacao) {
            case "RENTABILIDADE" -> Sort.by(Sort.Direction.DESC, "rentabilidadePercentual").and(porNome);
            case "MENOR_VALOR_MINIMO" -> Sort.by(Sort.Direction.ASC, "valorMinimo").and(porNome);
            case "MAIOR_LIQUIDEZ" -> JpaSort.unsafe(
                            Sort.Direction.DESC,
                            "case p.liquidez when 'DIARIA' then 3 when 'D_PLUS_N' then 2 when 'NO_VENCIMENTO' then 1 else 0 end")
                    .and(porNome);
            case "MENOR_RISCO" -> JpaSort.unsafe(
                            Sort.Direction.ASC,
                            "case p.nivelRisco when 'BAIXO' then 1 when 'MEDIO' then 2 when 'ALTO' then 3 else 4 end")
                    .and(porNome);
            default -> porNome;
        };
    }

    private String normalizarRisco(String risco) {
        String normalizado = normalizarTexto(risco);
        if (normalizado == null) return null;

        String valor = normalizado.toUpperCase(Locale.ROOT);
        if (!RISCOS_VALIDOS.contains(valor)) {
            throw erro("Nível de risco inválido. Use BAIXO, MEDIO ou ALTO.");
        }
        return valor;
    }

    private String normalizarOrdenacao(String ordenacao) {
        String normalizada = normalizarTexto(ordenacao);
        String valor = normalizada == null ? "NOME" : normalizada.toUpperCase(Locale.ROOT);
        if (!ORDENACOES_VALIDAS.contains(valor)) {
            throw erro("Ordenação inválida. Use NOME, RENTABILIDADE, MENOR_VALOR_MINIMO, MAIOR_LIQUIDEZ ou MENOR_RISCO.");
        }
        return valor;
    }

    private void validarPaginacao(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw erro("A paginação deve usar page maior ou igual a zero e size entre 1 e 50.");
        }
    }

    private String normalizarTexto(String valor) {
        if (valor == null) return null;
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
