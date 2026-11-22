package com.example.templatespring.validator;

import com.example.templatespring.dto.simulacao.request.SimulacaoRequest;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SimulacaoValidator {

    private static final int MAX_PRODUTOS = 4;

    private final ProdutoFinanceiroRepository produtoRepository;

    public SimulacaoValidator(ProdutoFinanceiroRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public void validar(SimulacaoRequest request) {
        if (request == null) {
            throw erro("Informe os dados da simulação.");
        }
        validarValor(request.getValorInicial(), "O aporte inicial");
        validarValor(request.getAporteMensal(), "O aporte mensal");
        if (request.getValorInicial().add(request.getAporteMensal()).compareTo(BigDecimal.ZERO) <= 0) {
            throw erro("Informe um aporte inicial ou mensal maior que zero.");
        }
        if (request.getPrazoMeses() <= 0 || request.getPrazoMeses() > 600) {
            throw erro("O prazo deve estar entre 1 e 600 meses.");
        }
        if (request.getPerfil() == null) {
            throw erro("Informe o perfil do investidor.");
        }

        List<UUID> ids = request.getProdutoIds();
        if (ids == null || ids.isEmpty()) {
            throw erro("Selecione pelo menos um produto.");
        }
        Set<UUID> distintos = new HashSet<>(ids);
        if (distintos.size() != ids.size()) {
            throw erro("Não selecione o mesmo produto mais de uma vez.");
        }
        if (distintos.size() > MAX_PRODUTOS) {
            throw erro("É possível comparar no máximo 4 produtos.");
        }

        List<ProdutoFinanceiro> produtos = produtoRepository.findAllById(distintos);
        if (produtos.size() != distintos.size() || produtos.stream().anyMatch(produto -> !produto.isAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Todos os produtos selecionados devem existir e estar ativos.");
        }
        for (ProdutoFinanceiro produto : produtos) {
            if (request.getValorInicial().compareTo(produto.getValorMinimo()) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "O aporte inicial está abaixo do valor mínimo de " + produto.getNome() + ".");
            }
        }
    }

    private void validarValor(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw erro(campo + " deve ser maior ou igual a zero.");
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
