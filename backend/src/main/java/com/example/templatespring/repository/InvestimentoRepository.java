package com.example.templatespring.repository;

import com.example.templatespring.entity.Investimento;
import com.example.templatespring.entity.enums.StatusInvestimento;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface InvestimentoRepository extends JpaRepository<Investimento, UUID> {

    List<Investimento> findByUsuarioIdAndStatusOrderByCreatedAtDesc(UUID usuarioId, StatusInvestimento status);

    @Lock(PESSIMISTIC_WRITE)
    @Query("select i from Investimento i where i.id = :id")
    Optional<Investimento> findByIdForUpdate(@Param("id") UUID id);
}
