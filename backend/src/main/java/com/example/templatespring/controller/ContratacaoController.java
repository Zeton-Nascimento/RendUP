package com.example.templatespring.controller;

import com.example.templatespring.dto.contratacao.request.ContratacaoRequest;
import com.example.templatespring.dto.contratacao.request.PreviewContratacaoRequest;
import com.example.templatespring.dto.contratacao.response.ContratacaoResponse;
import com.example.templatespring.dto.contratacao.response.PreviewContratacaoResponse;
import com.example.templatespring.service.ContratacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/contratacao", "/api/v1/investimentos"})
public class ContratacaoController {

    private final ContratacaoService contratacaoService;

    public ContratacaoController(ContratacaoService contratacaoService) {
        this.contratacaoService = contratacaoService;
    }

    @PreAuthorize("hasAuthority('ROLE_INVESTIDOR')")
    @PostMapping("/preview")
    public PreviewContratacaoResponse visualizarPrevia(
            @Valid @RequestBody PreviewContratacaoRequest request) {
        return contratacaoService.visualizarPrevia(request);
    }

    @PreAuthorize("hasAuthority('ROLE_INVESTIDOR')")
    @PostMapping
    public ResponseEntity<ContratacaoResponse> contratar(
            @Valid @RequestBody ContratacaoRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String chaveIdempotencia) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contratacaoService.contratar(request, chaveIdempotencia));
    }
}
