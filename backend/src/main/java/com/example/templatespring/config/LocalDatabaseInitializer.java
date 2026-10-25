package com.example.templatespring.config;

import com.example.templatespring.entity.security.Cargo;
import com.example.templatespring.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class LocalDatabaseInitializer implements CommandLineRunner {

    private final CargoRepository cargoRepository;

    @Override
    public void run(String... args) {
        garantirCargo("INVESTIDOR", "Investidor com acesso padrão");
        garantirCargo("ADMIN", "Administrador do sistema");
    }

    private void garantirCargo(String nome, String descricao) {
        cargoRepository.findByNome(nome).orElseGet(() -> cargoRepository.save(
                Cargo.builder().nome(nome).descricao(descricao).build()));
    }
}
