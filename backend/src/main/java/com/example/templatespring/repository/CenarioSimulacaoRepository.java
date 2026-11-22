package com.example.templatespring.repository;

import com.example.templatespring.entity.CenarioSimulacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenarioSimulacaoRepository extends JpaRepository<CenarioSimulacao, UUID> {

    List<CenarioSimulacao> findBySimulacaoIdOrderByRankingPosicaoAsc(UUID simulacaoId);
}
