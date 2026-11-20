package com.example.templatespring.repository;

import com.example.templatespring.entity.RegraFinanceira;
import com.example.templatespring.entity.enums.TipoProduto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegraFinanceiraRepository extends JpaRepository<RegraFinanceira, UUID> {

    Optional<RegraFinanceira> findByTipoProduto(TipoProduto tipoProduto);

    List<RegraFinanceira> findAllByOrderByTipoProdutoAsc();
}
