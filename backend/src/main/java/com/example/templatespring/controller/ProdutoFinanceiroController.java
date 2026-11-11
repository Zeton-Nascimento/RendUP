package com.example.templatespring.controller;

import com.example.templatespring.dto.produto.request.ItemCatalogoProdutoRequest;
import com.example.templatespring.dto.produto.request.ProdutoAdminFiltroParams;
import com.example.templatespring.dto.produto.request.ProdutoFinanceiroRequest;
import com.example.templatespring.dto.produto.request.ProdutoFiltroParams;
import com.example.templatespring.dto.produto.response.ProdutoFinanceiroResponse;
import com.example.templatespring.dto.produto.response.ResultadoImportacaoResponse;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import jakarta.validation.Valid;
import com.example.templatespring.service.ProdutoFinanceiroService;
import com.example.templatespring.service.UsoAbusivoRateLimitService;
import com.example.templatespring.service.catalogo.CatalogoSincronizacaoService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/produtos")
public class ProdutoFinanceiroController {

    private final ProdutoFinanceiroService service;
    private final CatalogoSincronizacaoService catalogoSincronizacaoService;
    private final UsoAbusivoRateLimitService usoAbusivoRateLimitService;

    public ProdutoFinanceiroController(
            ProdutoFinanceiroService service,
            CatalogoSincronizacaoService catalogoSincronizacaoService,
            UsoAbusivoRateLimitService usoAbusivoRateLimitService) {
        this.service = service;
        this.catalogoSincronizacaoService = catalogoSincronizacaoService;
        this.usoAbusivoRateLimitService = usoAbusivoRateLimitService;
    }

    @GetMapping
    public Page<ProdutoFinanceiroResponse> listar(
            @RequestParam(required = false) TipoProduto tipo,
            @RequestParam(required = false) String nivelRisco,
            @RequestParam(required = false) TipoLiquidez liquidez,
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "NOME") String ordenacao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return service.listarAtivos(new ProdutoFiltroParams(
                tipo, nivelRisco, liquidez, nome, ordenacao, page, size));
    }

    @PreAuthorize("hasAuthority('PRODUTO_GERIR')")
    @GetMapping("/admin")
    public Page<ProdutoFinanceiroResponse> listarAdmin(
            @RequestParam(required = false) TipoProduto tipo,
            @RequestParam(required = false) String nivelRisco,
            @RequestParam(required = false) TipoLiquidez liquidez,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.listarAdmin(new ProdutoAdminFiltroParams(
                tipo, nivelRisco, liquidez, nome, ativo, page, size));
    }

    @PreAuthorize("hasAuthority('PRODUTO_GERIR')")
    @PostMapping
    public ResponseEntity<ProdutoFinanceiroResponse> criarProduto(
            @Valid @RequestBody ProdutoFinanceiroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarProduto(request));
    }

    @PreAuthorize("hasAuthority('PRODUTO_GERIR')")
    @PutMapping("/{id}")
    public ProdutoFinanceiroResponse atualizarProduto(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoFinanceiroRequest request) {
        return service.atualizarProduto(id, request);
    }

    @PreAuthorize("hasAuthority('PRODUTO_GERIR')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativarProduto(@PathVariable UUID id) {
        service.desativarProduto(id);
    }

    @PreAuthorize("hasAuthority('PRODUTO_GERIR')")
    @PostMapping("/import")
    public ResultadoImportacaoResponse importarCatalogo(
            @RequestBody List<ItemCatalogoProdutoRequest> itens,
            HttpServletRequest request) {
        usoAbusivoRateLimitService.verificar("catalogo-importacao", consumidor(request));
        return service.importarCatalogo(itens);
    }

    @PreAuthorize("hasAuthority('PRODUTO_GERIR')")
    @PostMapping("/sincronizar")
    public ResultadoImportacaoResponse sincronizarCatalogo(HttpServletRequest request) {
        usoAbusivoRateLimitService.verificar("catalogo-sincronizacao", consumidor(request));
        return catalogoSincronizacaoService.sincronizar();
    }

    @GetMapping("/{id}")
    public ProdutoFinanceiroResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/relacionados")
    public List<ProdutoFinanceiroResponse> buscarRelacionados(@PathVariable UUID id) {
        return service.buscarRelacionados(id);
    }

    private String consumidor(HttpServletRequest request) {
        var autenticacao = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (autenticacao != null && autenticacao.isAuthenticated()
                && autenticacao.getName() != null && !autenticacao.getName().isBlank()) {
            return "usuario:" + autenticacao.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
