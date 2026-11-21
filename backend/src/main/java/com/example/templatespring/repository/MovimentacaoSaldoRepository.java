package com.example.templatespring.repository;

import com.example.templatespring.entity.MovimentacaoSaldo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoSaldoRepository extends JpaRepository<MovimentacaoSaldo, UUID> {
}
