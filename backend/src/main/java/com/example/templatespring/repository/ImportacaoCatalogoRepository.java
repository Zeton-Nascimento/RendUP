package com.example.templatespring.repository;

import com.example.templatespring.entity.ImportacaoCatalogo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportacaoCatalogoRepository extends JpaRepository<ImportacaoCatalogo, UUID> {
}
