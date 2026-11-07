package com.example.templatespring.repository;

import com.example.templatespring.entity.security.Permissao;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissaoRepository extends JpaRepository<Permissao, UUID> {

    Optional<Permissao> findByNome(String nome);
}
