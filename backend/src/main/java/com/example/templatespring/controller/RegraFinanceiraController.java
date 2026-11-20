package com.example.templatespring.controller;

import com.example.templatespring.dto.regra.RegraFinanceiraRequest;
import com.example.templatespring.dto.regra.RegraFinanceiraResponse;
import com.example.templatespring.dto.regra.ValidacaoRegraFinanceiraResponse;
import com.example.templatespring.entity.enums.TipoProduto;
import com.example.templatespring.service.RegraFinanceiraService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regras-financeiras")
@PreAuthorize("hasAuthority('REGRA_FINANCEIRA_GERIR')")
public class RegraFinanceiraController {

    private final RegraFinanceiraService service;

    public RegraFinanceiraController(RegraFinanceiraService service) {
        this.service = service;
    }

    @GetMapping
    public List<RegraFinanceiraResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{tipoProduto}")
    public RegraFinanceiraResponse buscar(@PathVariable TipoProduto tipoProduto) {
        return service.buscar(tipoProduto);
    }

    @PutMapping("/{tipoProduto}")
    public RegraFinanceiraResponse salvar(
            @PathVariable TipoProduto tipoProduto,
            @Valid @RequestBody RegraFinanceiraRequest request) {
        return service.salvar(tipoProduto, request);
    }

    @GetMapping("/{tipoProduto}/validacao")
    public ValidacaoRegraFinanceiraResponse validar(@PathVariable TipoProduto tipoProduto) {
        return service.validar(tipoProduto);
    }
}
