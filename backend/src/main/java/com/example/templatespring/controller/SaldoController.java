package com.example.templatespring.controller;

import com.example.templatespring.dto.saldo.response.SaldoResponse;
import com.example.templatespring.service.DepositoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/saldo")
public class SaldoController {

    private final DepositoService depositoService;

    public SaldoController(DepositoService depositoService) {
        this.depositoService = depositoService;
    }

    @PreAuthorize("hasAuthority('ROLE_INVESTIDOR')")
    @GetMapping
    public ResponseEntity<SaldoResponse> obterSaldo() {
        return ResponseEntity.ok(depositoService.obterSaldo());
    }
}
