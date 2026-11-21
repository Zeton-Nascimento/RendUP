package com.example.templatespring.repository;

import com.example.templatespring.entity.OperacaoIdempotente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface OperacaoIdempotenteRepository extends JpaRepository<OperacaoIdempotente, UUID> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            select o
            from OperacaoIdempotente o
            where o.usuarioId = :usuarioId
              and o.chaveIdempotencia = :chaveIdempotencia
            """)
    Optional<OperacaoIdempotente> findByUsuarioIdAndChaveIdempotenciaForUpdate(
            @Param("usuarioId") UUID usuarioId,
            @Param("chaveIdempotencia") String chaveIdempotencia);
}
