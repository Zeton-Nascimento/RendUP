package com.example.templatespring.controller;

import com.example.templatespring.dto.resgate.response.ResgateInvestimentoResponse;
import com.example.templatespring.service.ResgateService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/investimentos")
public class InvestimentosResgataveisController {

    private final ResgateService resgateService;

    public InvestimentosResgataveisController(ResgateService resgateService) {
        this.resgateService = resgateService;
    }

    @PreAuthorize("hasAuthority('ROLE_INVESTIDOR')")
    @GetMapping("/resgataveis")
    public List<ResgateInvestimentoResponse> listarDisponiveis() {
        return resgateService.listarDisponiveis();
    }
}
