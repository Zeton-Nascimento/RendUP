package com.example.templatespring.controller;

import com.example.templatespring.dto.resgate.request.PreviewResgateRequest;
import com.example.templatespring.dto.resgate.request.ResgateRequest;
import com.example.templatespring.dto.resgate.response.PreviewResgateResponse;
import com.example.templatespring.dto.resgate.response.ResgateInvestimentoResponse;
import com.example.templatespring.dto.resgate.response.ResgateResponse;
import com.example.templatespring.service.ResgateService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/investimentos/{investimentoId}/resgate")
public class ResgateController {

    private final ResgateService resgateService;

    public ResgateController(ResgateService resgateService) {
        this.resgateService = resgateService;
    }

    @PreAuthorize("hasAuthority('ROLE_INVESTIDOR')")
    @GetMapping
    public ResgateInvestimentoResponse consultar(@PathVariable UUID investimentoId) {
        return resgateService.consultar(investimentoId);
    }

    @PreAuthorize("hasAuthority('ROLE_INVESTIDOR')")
    @PostMapping("/preview")
    public PreviewResgateResponse visualizarPrevia(
            @PathVariable UUID investimentoId,
            @Valid @RequestBody(required = false) PreviewResgateRequest request) {
        return resgateService.visualizarPrevia(investimentoId, request);
    }

    @PreAuthorize("hasAuthority('ROLE_INVESTIDOR')")
    @PostMapping
    public ResponseEntity<ResgateResponse> confirmar(
            @PathVariable UUID investimentoId,
            @Valid @RequestBody ResgateRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String chaveIdempotencia) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(resgateService.resgatar(investimentoId, request, chaveIdempotencia));
    }
}
