package com.example.templatespring.controller;

import com.example.templatespring.dto.deposito.request.DepositoRequest;
import com.example.templatespring.dto.deposito.response.DepositoResponse;
import com.example.templatespring.service.DepositoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/depositos")
public class DepositoController {

    private final DepositoService depositoService;

    public DepositoController(DepositoService depositoService) {
        this.depositoService = depositoService;
    }

    @PreAuthorize("hasAuthority('ROLE_INVESTIDOR')")
    @PostMapping
    public ResponseEntity<DepositoResponse> depositar(
            @Valid @RequestBody DepositoRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String chaveIdempotencia) {
        return ResponseEntity.ok(depositoService.depositar(request, chaveIdempotencia));
    }
}
