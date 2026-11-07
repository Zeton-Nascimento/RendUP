package com.example.templatespring.service;

import com.example.templatespring.dto.produto.request.ItemCatalogoProdutoRequest;
import com.example.templatespring.dto.produto.request.ProdutoAdminFiltroParams;
import com.example.templatespring.dto.produto.request.ProdutoFinanceiroRequest;
import com.example.templatespring.dto.produto.request.ProdutoFiltroParams;
import com.example.templatespring.dto.produto.response.ImportacaoErroResponse;
import com.example.templatespring.dto.produto.response.ProdutoFinanceiroResponse;
import com.example.templatespring.dto.produto.response.ResultadoImportacaoResponse;
import com.example.templatespring.entity.ImportacaoCatalogo;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.entity.enums.TipoProduto;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.mapper.ProdutoFinanceiroMapper;
import com.example.templatespring.repository.ImportacaoCatalogoRepository;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import com.example.templatespring.validator.ProdutoFinanceiroAdminValidator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class ProdutoFinanceiroService {

    private static final String PRODUTO_NAO_ENCONTRADO = "Produto financeiro não encontrado.";
    private static final Set<String> RISCOS_VALIDOS = Set.of("BAIXO", "MEDIO", "ALTO");
    private static final Set<String> ORDENACOES_VALIDAS = Set.of(
            "NOME", "RENTABILIDADE", "MENOR_VALOR_MINIMO", "MAIOR_LIQUIDEZ", "MENOR_RISCO");

    private final ProdutoFinanceiroRepository repository;
    private final ProdutoFinanceiroAdminValidator validator;
    private final ImportacaoCatalogoRepository importacaoRepository;

    public ProdutoFinanceiroService(
            ProdutoFinanceiroRepository repository,
            ProdutoFinanceiroAdminValidator validator,
            ImportacaoCatalogoRepository importacaoRepository) {
        this.repository = repository;
        this.validator = validator;
        this.importacaoRepository = importacaoRepository;
    }

    public Page<ProdutoFinanceiroResponse> listarAtivos(ProdutoFiltroParams filtro) {
        String risco = normalizarRisco(filtro.nivelRisco());
        String ordenacao = normalizarOrdenacao(filtro.ordenacao());
        validarPaginacao(filtro.page(), filtro.size());

        Pageable pageable = PageRequest.of(filtro.page(), filtro.size(), montarOrdenacao(ordenacao));
        String nome = normalizarTexto(filtro.nome());

        return repository.buscarAtivosFiltrados(filtro.tipo(), risco, filtro.liquidez(), nome, pageable)
                .map(ProdutoFinanceiroMapper::toResponse);
    }

    public Page<ProdutoFinanceiroResponse> listarAdmin(ProdutoAdminFiltroParams filtro) {
        String risco = normalizarRisco(filtro.nivelRisco());
        validarPaginacao(filtro.page(), filtro.size());
        Pageable pageable = PageRequest.of(
                filtro.page(),
                filtro.size(),
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.ASC, "nome")));

        return repository.buscarAdminFiltrados(
                        filtro.tipo(), risco, filtro.liquidez(), normalizarTexto(filtro.nome()), filtro.ativo(), pageable)
                .map(ProdutoFinanceiroMapper::toResponse);
    }

    public ProdutoFinanceiroResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .filter(ProdutoFinanceiro::isAtivo)
                .map(ProdutoFinanceiroMapper::toResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException(PRODUTO_NAO_ENCONTRADO));
    }

    public List<ProdutoFinanceiroResponse> buscarRelacionados(UUID id) {
        ProdutoFinanceiro produto = repository.findById(id)
                .filter(ProdutoFinanceiro::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException(PRODUTO_NAO_ENCONTRADO));

        return repository.findByTipoProdutoAndAtivoTrueAndIdNot(
                        produto.getTipoProduto(), id, PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "nome")))
                .stream()
                .map(ProdutoFinanceiroMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProdutoFinanceiroResponse criarProduto(ProdutoFinanceiroRequest request) {
        ProdutoFinanceiroRequest validado = validator.validar(request);
        validarDuplicidade(validado.getNome(), validado.getTipoProduto(), null);

        ProdutoFinanceiro produto = ProdutoFinanceiroMapper.toEntity(validado);
        LocalDateTime agora = LocalDateTime.now();
        produto.setCreatedAt(agora);
        produto.setUpdatedAt(agora);
        return ProdutoFinanceiroMapper.toResponse(repository.save(produto));
    }

    @Transactional
    public ProdutoFinanceiroResponse atualizarProduto(UUID id, ProdutoFinanceiroRequest request) {
        ProdutoFinanceiroRequest validado = validator.validar(request);
        ProdutoFinanceiro produto = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(PRODUTO_NAO_ENCONTRADO));
        validarDuplicidade(validado.getNome(), validado.getTipoProduto(), id);

        ProdutoFinanceiroMapper.atualizarEntity(produto, validado);
        produto.setUpdatedAt(LocalDateTime.now());
        return ProdutoFinanceiroMapper.toResponse(repository.save(produto));
    }

    @Transactional
    public void desativarProduto(UUID id) {
        ProdutoFinanceiro produto = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(PRODUTO_NAO_ENCONTRADO));
        if (!produto.isAtivo()) {
            return;
        }

        produto.setAtivo(false);
        produto.setUpdatedAt(LocalDateTime.now());
        repository.save(produto);
    }

    @Transactional
    public ResultadoImportacaoResponse importarCatalogo(List<ItemCatalogoProdutoRequest> itens) {
        List<ItemCatalogoProdutoRequest> entrada = itens == null ? List.of() : itens;
        List<ImportacaoErroResponse> erros = new ArrayList<>();
        int inseridos = 0;
        int atualizados = 0;

        for (int indice = 0; indice < entrada.size(); indice++) {
            ItemCatalogoProdutoRequest item = entrada.get(indice);
            try {
                validator.validarImportacao(item);
            } catch (ResponseStatusException exception) {
                erros.add(ImportacaoErroResponse.builder()
                        .linha(indice + 1)
                        .motivo(exception.getReason() == null ? "Item inválido." : exception.getReason())
                        .build());
                continue;
            }

            Optional<ProdutoFinanceiro> existente = buscarExistente(item);
            if (existente.isPresent()) {
                ProdutoFinanceiro produto = existente.get();
                ProdutoFinanceiroMapper.atualizarEntity(produto, item);
                produto.setUpdatedAt(LocalDateTime.now());
                repository.save(produto);
                atualizados++;
            } else {
                ProdutoFinanceiro produto = new ProdutoFinanceiro();
                ProdutoFinanceiroMapper.atualizarEntity(produto, item);
                LocalDateTime agora = LocalDateTime.now();
                produto.setCreatedAt(agora);
                produto.setUpdatedAt(agora);
                repository.save(produto);
                inseridos++;
            }
        }

        String versao = entrada.stream()
                .map(ItemCatalogoProdutoRequest::getVersaoCatalogo)
                .map(this::normalizarTexto)
                .filter(valor -> valor != null)
                .findFirst()
                .orElse(null);
        String hash = calcularHash(entrada);

        ImportacaoCatalogo auditoria = new ImportacaoCatalogo();
        auditoria.setDataImportacao(LocalDateTime.now());
        auditoria.setTotalItens(entrada.size());
        auditoria.setInseridos(inseridos);
        auditoria.setAtualizados(atualizados);
        auditoria.setRejeitados(erros.size());
        auditoria.setUsuarioResponsavel(obterUsuarioAtual());
        auditoria.setVersaoCatalogo(versao);
        auditoria.setHashImportacao(hash);
        auditoria.setErros(erros.isEmpty() ? null : erros.stream()
                .map(erro -> "linha " + erro.getLinha() + ": " + erro.getMotivo())
                .reduce((a, b) -> a + "\n" + b)
                .orElse(null));
        importacaoRepository.save(auditoria);

        return ResultadoImportacaoResponse.builder()
                .importacaoId(auditoria.getId())
                .dataImportacao(auditoria.getDataImportacao())
                .totalItens(entrada.size())
                .inseridos(inseridos)
                .atualizados(atualizados)
                .rejeitados(erros.size())
                .versaoCatalogo(versao)
                .hashImportacao(hash)
                .erros(erros)
                .build();
    }

    private Optional<ProdutoFinanceiro> buscarExistente(ItemCatalogoProdutoRequest item) {
        if (item.getOrigemCatalogo() != null && item.getCodigoExterno() != null) {
            Optional<ProdutoFinanceiro> porCodigo = repository.findByOrigemCatalogoAndCodigoExterno(
                    item.getOrigemCatalogo(), item.getCodigoExterno());
            if (porCodigo.isPresent()) {
                return porCodigo;
            }
        }
        return repository.findByNomeIgnoreCaseAndTipoProduto(item.getNome(), item.getTipoProduto());
    }

    private void validarDuplicidade(String nome, TipoProduto tipo, UUID idAtual) {
        Optional<ProdutoFinanceiro> existente = idAtual == null
                ? repository.findByNomeIgnoreCaseAndTipoProduto(nome, tipo)
                : repository.findByNomeIgnoreCaseAndTipoProdutoAndIdNot(nome, tipo, idAtual);
        if (existente.isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Já existe um produto com esse nome e tipo.");
        }
    }

    private String obterUsuarioAtual() {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof JwtAuthenticationToken jwt) {
            String email = jwt.getToken().getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        return autenticacao == null ? "sistema" : autenticacao.getName();
    }

    private String calcularHash(List<ItemCatalogoProdutoRequest> itens) {
        String conteudo = itens.stream()
                .map(item -> String.join("|",
                        String.valueOf(item == null ? null : item.getTipoProduto()),
                        item == null ? "" : String.valueOf(item.getNome()),
                        item == null ? "" : String.valueOf(item.getCodigoExterno()),
                        item == null ? "" : String.valueOf(item.getVersaoCatalogo()),
                        item == null ? "" : String.valueOf(item.getPayloadOriginal())))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(conteudo.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Não foi possível gerar o hash da importação.", exception);
        }
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
            throw new ResponseStatusException(BAD_REQUEST, "Nível de risco inválido. Use BAIXO, MEDIO ou ALTO.");
        }
        return valor;
    }

    private String normalizarOrdenacao(String ordenacao) {
        String normalizada = normalizarTexto(ordenacao);
        String valor = normalizada == null ? "NOME" : normalizada.toUpperCase(Locale.ROOT);
        if (!ORDENACOES_VALIDAS.contains(valor)) {
            throw new ResponseStatusException(BAD_REQUEST, "Ordenação inválida.");
        }
        return valor;
    }

    private void validarPaginacao(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(BAD_REQUEST, "A paginação deve usar page maior ou igual a zero e size entre 1 e 50.");
        }
    }

    private String normalizarTexto(String valor) {
        if (valor == null) return null;
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }
}
