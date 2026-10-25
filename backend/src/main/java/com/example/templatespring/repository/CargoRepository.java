package com.example.templatespring.repository;

import com.example.templatespring.entity.security.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CargoRepository extends JpaRepository<Cargo, UUID> {

    Optional<Cargo> findByNome(String nome);
}
