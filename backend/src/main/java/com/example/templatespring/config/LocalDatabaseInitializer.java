package com.example.templatespring.config;

import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.security.Cargo;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.repository.CargoRepository;
import com.example.templatespring.repository.UsuarioRepository;
import java.math.BigDecimal;
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
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        garantirCargo("INVESTIDOR", "Investidor com acesso padrão");
        Cargo cargoAdmin = garantirCargo("ADMIN", "Administrador do sistema");
        garantirUsuarioAdmin(cargoAdmin);
    }

    private Cargo garantirCargo(String nome, String descricao) {
        return cargoRepository.findByNome(nome).orElseGet(() -> cargoRepository.save(
                Cargo.builder().nome(nome).descricao(descricao).build()));
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
}
