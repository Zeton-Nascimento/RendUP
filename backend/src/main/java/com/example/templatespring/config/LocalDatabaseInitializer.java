package com.example.templatespring.config;

import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.enums.Indexador;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.entity.security.Cargo;
import com.example.templatespring.entity.security.Permissao;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.repository.CargoRepository;
import com.example.templatespring.repository.PermissaoRepository;
import com.example.templatespring.repository.ProdutoFinanceiroRepository;
import com.example.templatespring.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class LocalDatabaseInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@investimentos.com";
    private static final String ADMIN_SENHA = "HDzcVpSOYsG+aZAE";

    private final CargoRepository cargoRepository;
    private final PermissaoRepository permissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoFinanceiroRepository produtoFinanceiroRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        garantirCargo("INVESTIDOR", "Investidor com acesso padrão");
        Cargo cargoAdmin = garantirCargo("ADMIN", "Administrador do sistema");
        Permissao gerirProdutos = garantirPermissao("PRODUTO_GERIR", "Gerenciar produtos financeiros");
        cargoAdmin.getPermissoes().add(gerirProdutos);
        cargoRepository.save(cargoAdmin);
        garantirUsuarioAdmin(cargoAdmin);
        garantirProdutosParaDemonstracao();
    }

    private Cargo garantirCargo(String nome, String descricao) {
        return cargoRepository.findByNome(nome).orElseGet(() -> cargoRepository.save(
                Cargo.builder().nome(nome).descricao(descricao).build()));
    }

    private Permissao garantirPermissao(String nome, String descricao) {
        return permissaoRepository.findByNome(nome).orElseGet(() -> permissaoRepository.save(
                Permissao.builder().nome(nome).descricao(descricao).build()));
    }

    private void garantirUsuarioAdmin(Cargo cargoAdmin) {
        Usuario usuario = usuarioRepository.findByEmail(ADMIN_EMAIL).orElseGet(Usuario::new);

        if (usuario.getId() == null) {
            usuario.setNome("Administrador");
            usuario.setEmail(ADMIN_EMAIL);
            usuario.setSenha(passwordEncoder.encode(ADMIN_SENHA));
            usuario.setPerfilInvestidor(PerfilInvestidor.CONSERVADOR);
            usuario.setAtivo(true);
            usuario.setSaldoDisponivel(BigDecimal.ZERO);
        }

        usuario.adicionarCargo(cargoAdmin);
        usuarioRepository.save(usuario);
    }

    private void garantirProdutosParaDemonstracao() {
        if (produtoFinanceiroRepository.count() > 0) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        produtoFinanceiroRepository.saveAll(List.of(
                produto(TipoProduto.CDB, "CDB Liquidez Diária", "Renda fixa para manter seu dinheiro acessível.",
                        "BAIXO", TipoLiquidez.DIARIA, 10.50, 100.00, 0, 0, agora),
                produto(TipoProduto.LCI, "LCI Mais Rendimento", "Investimento isento de IR para pessoa física.",
                        "BAIXO", TipoLiquidez.NO_VENCIMENTO, 11.20, 500.00, 365, 90, agora),
                produto(TipoProduto.TESOURO_SELIC, "Tesouro Selic 2029", "Título público para objetivos de médio prazo.",
                        "BAIXO", TipoLiquidez.D_PLUS_N, 12.00, 150.00, 2, 730, agora),
                produto(TipoProduto.FUNDO, "Fundo Balanceado RendUP", "Carteira diversificada para quem busca equilíbrio.",
                        "MEDIO", TipoLiquidez.D_PLUS_N, 14.00, 250.00, 5, 365, agora),
                produto(TipoProduto.ACAO, "Carteira de Ações Brasil", "Exposição diversificada ao mercado de ações.",
                        "ALTO", TipoLiquidez.D_PLUS_N, 18.00, 100.00, 3, 0, agora),
                produto(TipoProduto.POUPANCA, "Poupança RendUP", "Opção simples para reserva de curto prazo.",
                        "BAIXO", TipoLiquidez.DIARIA, 6.00, 10.00, 0, 0, agora)));
    }

    private ProdutoFinanceiro produto(
            TipoProduto tipo,
            String nome,
            String descricao,
            String risco,
            TipoLiquidez liquidez,
            double rentabilidade,
            double valorMinimo,
            int diasLiquidez,
            int prazoDias,
            LocalDateTime agora) {
        ProdutoFinanceiro produto = new ProdutoFinanceiro();
        produto.setTipoProduto(tipo);
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setIndexador(indexadorPara(tipo));
        produto.setPercentualIndexador(percentualIndexadorPara(tipo));
        produto.setCarenciaDias(carenciaPara(tipo));
        produto.setTaxas(taxasPara(tipo));
        produto.setTributacao(tributacaoPara(tipo));
        produto.setNivelRisco(risco);
        produto.setLiquidez(liquidez);
        produto.setRentabilidadePercentual(BigDecimal.valueOf(rentabilidade));
        produto.setValorMinimo(BigDecimal.valueOf(valorMinimo));
        produto.setDiasLiquidez(diasLiquidez);
        produto.setPrazoDias(prazoDias);
        produto.setAtivo(true);
        produto.setCreatedAt(agora);
        produto.setUpdatedAt(agora);
        return produto;
    }

    private Indexador indexadorPara(TipoProduto tipo) {
        return switch (tipo) {
            case CDB, LCI, LCA, LC, FUNDO -> Indexador.CDI;
            case TESOURO_SELIC, POUPANCA -> Indexador.SELIC;
            case ACAO -> Indexador.IBOVESPA;
        };
    }

    private BigDecimal percentualIndexadorPara(TipoProduto tipo) {
        return switch (tipo) {
            case CDB -> BigDecimal.valueOf(100);
            case LCI -> BigDecimal.valueOf(95);
            case LCA -> BigDecimal.valueOf(98);
            case LC -> BigDecimal.valueOf(110);
            case TESOURO_SELIC -> BigDecimal.valueOf(100);
            case POUPANCA, FUNDO, ACAO -> null;
        };
    }

    private String taxasPara(TipoProduto tipo) {
        return switch (tipo) {
            case FUNDO -> "Taxa de administração conforme o fundo";
            case TESOURO_SELIC -> "Taxa de custódia conforme a regra do produto";
            case ACAO -> "Taxas de corretagem e custódia conforme a operação";
            default -> "Sem taxa de administração informada";
        };
    }

    private Integer carenciaPara(TipoProduto tipo) {
        return switch (tipo) {
            case CDB -> 30;
            case LCI -> 90;
            case LCA -> 120;
            case LC -> 60;
            case TESOURO_SELIC, POUPANCA, ACAO -> 0;
            case FUNDO -> 30;
        };
    }

    private String tributacaoPara(TipoProduto tipo) {
        return switch (tipo) {
            case LCI, LCA, POUPANCA -> "Isento de IR para pessoa física";
            case CDB, LC, TESOURO_SELIC, FUNDO -> "IR conforme prazo e regra do produto";
            case ACAO -> "IR conforme a operação";
        };
    }
}
