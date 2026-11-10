package com.example.templatespring.service.catalogo;

import com.example.templatespring.dto.produto.request.ItemCatalogoProdutoRequest;
import com.example.templatespring.dto.produto.response.CatalogoFonteResponse;
import com.example.templatespring.dto.produto.response.ImportacaoErroResponse;
import com.example.templatespring.dto.produto.response.ResultadoImportacaoResponse;
import com.example.templatespring.entity.ImportacaoCatalogo;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.exception.CatalogoFonteException;
import com.example.templatespring.mapper.ProdutoFinanceiroMapper;
import com.example.templatespring.repository.ImportacaoCatalogoRepository;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import com.example.templatespring.validator.ProdutoFinanceiroAdminValidator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogoSincronizacaoService {

    private static final String ORIGEM_PADRAO = "MOCK_CATALOGO_EXTERNO";

    private final CatalogoFonteClient fonteClient;
    private final ProdutoFinanceiroRepository produtoRepository;
    private final ProdutoFinanceiroAdminValidator validator;
    private final ImportacaoCatalogoRepository importacaoRepository;

    @Value("${catalogo.sincronizacao.tentativas:2}")
    private int tentativas;

    public CatalogoSincronizacaoService(
            CatalogoFonteClient fonteClient,
            ProdutoFinanceiroRepository produtoRepository,
            ProdutoFinanceiroAdminValidator validator,
            ImportacaoCatalogoRepository importacaoRepository) {
        this.fonteClient = fonteClient;
        this.produtoRepository = produtoRepository;
        this.validator = validator;
        this.importacaoRepository = importacaoRepository;
    }

    @Transactional
    public ResultadoImportacaoResponse sincronizar() {
        CatalogoFonteResponse fonte;
        try {
            fonte = buscarComRetry();
        } catch (CatalogoFonteException exception) {
            return registrarFalha(
                    0,
                    null,
                    null,
                    "",
                    List.of(erro(0, exception.getMessage())),
                    exception.getMessage());
        }

        String origem = normalizar(fonte == null ? null : fonte.getOrigem());
        String versao = normalizar(fonte == null ? null : fonte.getVersao());
        List<ItemCatalogoProdutoRequest> itens = fonte == null || fonte.getItens() == null
                ? List.of()
                : fonte.getItens();
        List<ImportacaoErroResponse> erros = new ArrayList<>();

        if (origem == null) {
            erros.add(erro(0, "A origem do catálogo é obrigatória."));
        }
        if (versao == null) {
            erros.add(erro(0, "A versão do catálogo é obrigatória."));
        }
        if (itens.isEmpty()) {
            erros.add(erro(0, "O catálogo deve conter pelo menos um produto."));
        }

        Set<String> chavesRecebidas = new HashSet<>();
        for (int indice = 0; indice < itens.size(); indice++) {
            ItemCatalogoProdutoRequest item = itens.get(indice);
            try {
                if (item == null) {
                    throw new ResponseStatusException(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "Item de catálogo inválido.");
                }

                item.setOrigemCatalogo(origem == null ? ORIGEM_PADRAO : origem);
                item.setVersaoCatalogo(versao);
                validator.validarImportacao(item);

                String chave = chaveDoItem(item);
                if (!chavesRecebidas.add(chave)) {
                    throw new ResponseStatusException(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "Produto repetido no catálogo recebido.");
                }
            } catch (ResponseStatusException exception) {
                erros.add(erro(indice + 1, exception.getReason() == null ? "Item inválido." : exception.getReason()));
            }
        }

        String hash = calcularHash(origem, versao, itens);
        if (!erros.isEmpty()) {
            return registrarFalha(
                    itens.size(),
                    origem,
                    versao,
                    hash,
                    erros,
                    "O catálogo não foi aplicado porque contém dados inválidos.");
        }

        int inseridos = 0;
        int atualizados = 0;
        int inativados = 0;
        LocalDateTime agora = LocalDateTime.now();

        for (ItemCatalogoProdutoRequest item : itens) {
            Optional<ProdutoFinanceiro> existente = buscarExistente(item);
            if (existente.isPresent()) {
                ProdutoFinanceiro produto = existente.get();
                ProdutoFinanceiroMapper.atualizarEntity(produto, item);
                produto.setUpdatedAt(agora);
                produtoRepository.save(produto);
                atualizados++;
            } else {
                ProdutoFinanceiro produto = new ProdutoFinanceiro();
                ProdutoFinanceiroMapper.atualizarEntity(produto, item);
                produto.setCreatedAt(agora);
                produto.setUpdatedAt(agora);
                produtoRepository.save(produto);
                inseridos++;
            }
        }

        Set<String> codigosRecebidos = itens.stream()
                .map(ItemCatalogoProdutoRequest::getCodigoExterno)
                .filter(this::preenchido)
                .collect(java.util.stream.Collectors.toSet());
        for (ProdutoFinanceiro produto : produtoRepository.findByOrigemCatalogo(origem)) {
            if (preenchido(produto.getCodigoExterno()) && !codigosRecebidos.contains(produto.getCodigoExterno())) {
                produto.setAtivo(false);
                produto.setUpdatedAt(agora);
                produtoRepository.save(produto);
                inativados++;
            }
        }

        ImportacaoCatalogo auditoria = salvarAuditoria(
                itens.size(), inseridos, atualizados, inativados, 0,
                origem, versao, hash, "SUCESSO", "Catálogo sincronizado com sucesso.", null);

        return ResultadoImportacaoResponse.builder()
                .importacaoId(auditoria.getId())
                .dataImportacao(auditoria.getDataImportacao())
                .totalItens(itens.size())
                .inseridos(inseridos)
                .atualizados(atualizados)
                .inativados(inativados)
                .rejeitados(0)
                .versaoCatalogo(versao)
                .origemCatalogo(origem)
                .hashImportacao(hash)
                .status("SUCESSO")
                .mensagem("Catálogo sincronizado com sucesso.")
                .erros(List.of())
                .build();
    }

    private CatalogoFonteResponse buscarComRetry() {
        CatalogoFonteException ultimaFalha = null;
        int quantidadeTentativas = Math.max(1, tentativas);
        for (int tentativa = 0; tentativa < quantidadeTentativas; tentativa++) {
            try {
                CatalogoFonteResponse resposta = fonteClient.buscarCatalogo();
                if (resposta == null) {
                    throw new CatalogoFonteException("A fonte retornou um catálogo vazio.");
                }
                return resposta;
            } catch (CatalogoFonteException exception) {
                ultimaFalha = exception;
            }
        }
        throw ultimaFalha == null
                ? new CatalogoFonteException("Não foi possível consultar o catálogo.")
                : ultimaFalha;
    }

    private ResultadoImportacaoResponse registrarFalha(
            int total,
            String origem,
            String versao,
            String hash,
            List<ImportacaoErroResponse> erros,
            String mensagem) {
        List<ImportacaoErroResponse> errosSeguros = erros == null ? List.of() : erros;
        ImportacaoCatalogo auditoria = salvarAuditoria(
                total, 0, 0, 0, errosSeguros.size(), origem, versao, hash,
                "FALHA", mensagem, errosSeguros);

        return ResultadoImportacaoResponse.builder()
                .importacaoId(auditoria.getId())
                .dataImportacao(auditoria.getDataImportacao())
                .totalItens(total)
                .inseridos(0)
                .atualizados(0)
                .inativados(0)
                .rejeitados(errosSeguros.size())
                .versaoCatalogo(versao)
                .origemCatalogo(origem)
                .hashImportacao(hash)
                .status("FALHA")
                .mensagem(mensagem)
                .erros(errosSeguros)
                .build();
    }

    private ImportacaoCatalogo salvarAuditoria(
            int total,
            int inseridos,
            int atualizados,
            int inativados,
            int rejeitados,
            String origem,
            String versao,
            String hash,
            String status,
            String mensagem,
            List<ImportacaoErroResponse> erros) {
        ImportacaoCatalogo auditoria = new ImportacaoCatalogo();
        auditoria.setDataImportacao(LocalDateTime.now());
        auditoria.setTotalItens(total);
        auditoria.setInseridos(inseridos);
        auditoria.setAtualizados(atualizados);
        auditoria.setInativados(inativados);
        auditoria.setRejeitados(rejeitados);
        auditoria.setUsuarioResponsavel(obterUsuarioAtual());
        auditoria.setOrigemCatalogo(origem);
        auditoria.setVersaoCatalogo(versao);
        auditoria.setHashImportacao(hash);
        auditoria.setStatus(status);
        auditoria.setMensagem(mensagem);
        auditoria.setErros(formatarErros(erros));
        return importacaoRepository.save(auditoria);
    }

    private Optional<ProdutoFinanceiro> buscarExistente(ItemCatalogoProdutoRequest item) {
        if (preenchido(item.getOrigemCatalogo()) && preenchido(item.getCodigoExterno())) {
            Optional<ProdutoFinanceiro> porCodigo = produtoRepository.findByOrigemCatalogoAndCodigoExterno(
                    item.getOrigemCatalogo(), item.getCodigoExterno());
            if (porCodigo.isPresent()) {
                return porCodigo;
            }
        }
        return produtoRepository.findByNomeIgnoreCaseAndTipoProduto(item.getNome(), item.getTipoProduto());
    }

    private String chaveDoItem(ItemCatalogoProdutoRequest item) {
        String codigo = normalizar(item.getCodigoExterno());
        return (codigo == null ? item.getNome() : codigo).toLowerCase(Locale.ROOT)
                + "|" + item.getTipoProduto();
    }

    private String calcularHash(String origem, String versao, List<ItemCatalogoProdutoRequest> itens) {
        String conteudo = origem + "|" + versao + "\n" + itens.stream()
                .filter(item -> item != null)
                .map(item -> String.join("|",
                        String.valueOf(item.getTipoProduto()),
                        String.valueOf(item.getNome()),
                        String.valueOf(item.getCodigoExterno()),
                        String.valueOf(item.getDescricao()),
                        String.valueOf(item.getRentabilidadePercentual()),
                        String.valueOf(item.getValorMinimo()),
                        String.valueOf(item.isAtivo())))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(conteudo.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Não foi possível gerar o hash do catálogo.", exception);
        }
    }

    private String formatarErros(List<ImportacaoErroResponse> erros) {
        if (erros == null || erros.isEmpty()) {
            return null;
        }
        return erros.stream()
                .map(erro -> "linha " + erro.getLinha() + ": " + erro.getMotivo())
                .reduce((a, b) -> a + "\n" + b)
                .orElse(null);
    }

    private ImportacaoErroResponse erro(int linha, String motivo) {
        return ImportacaoErroResponse.builder()
                .linha(linha)
                .motivo(motivo == null ? "Falha ao consultar o catálogo." : motivo)
                .build();
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

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private boolean preenchido(String valor) {
        return normalizar(valor) != null;
    }
}
