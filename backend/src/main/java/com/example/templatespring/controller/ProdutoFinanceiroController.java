package com.example.templatespring.controller;

import com.example.templatespring.dto.produto.request.ProdutoFiltroParams;
import com.example.templatespring.dto.produto.response.ProdutoFinanceiroResponse;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import com.example.templatespring.service.ProdutoFinanceiroService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/produtos")
public class ProdutoFinanceiroController {

    private final ProdutoFinanceiroService service;

    public ProdutoFinanceiroController(ProdutoFinanceiroService service) {
        this.service = service;
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

    @GetMapping("/{id}")
    public ProdutoFinanceiroResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/relacionados")
    public List<ProdutoFinanceiroResponse> buscarRelacionados(@PathVariable UUID id) {
        return service.buscarRelacionados(id);
    }
}
