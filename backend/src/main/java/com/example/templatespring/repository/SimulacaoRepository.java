package com.example.templatespring.repository;

import com.example.templatespring.entity.Simulacao;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulacaoRepository extends JpaRepository<Simulacao, UUID> {

    Optional<Simulacao> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
