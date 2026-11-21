package com.example.templatespring.repository;

import com.example.templatespring.entity.security.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select distinct u
            from Usuario u
            left join fetch u.cargos c
            left join fetch c.permissoes
            left join fetch u.dadosPessoais
            left join fetch u.conta
            where lower(u.email) = lower(:email)
            """)
    Optional<Usuario> findByEmailComCargosEPermissoes(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);
}
