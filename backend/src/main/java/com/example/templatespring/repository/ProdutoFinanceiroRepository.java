package com.example.templatespring.repository;

import com.example.templatespring.entity.ProdutoFinanceiro;
import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoFinanceiroRepository extends JpaRepository<ProdutoFinanceiro, UUID> {

    Optional<ProdutoFinanceiro> findByNomeIgnoreCaseAndTipoProduto(String nome, TipoProduto tipoProduto);

    Optional<ProdutoFinanceiro> findByNomeIgnoreCaseAndTipoProdutoAndIdNot(
            String nome, TipoProduto tipoProduto, UUID id);

    Optional<ProdutoFinanceiro> findByOrigemCatalogoAndCodigoExterno(String origemCatalogo, String codigoExterno);

    List<ProdutoFinanceiro> findByTipoProdutoAndAtivoTrueAndIdNot(
            TipoProduto tipoProduto, UUID id, Pageable pageable);

    @Query("""
            select p
            from ProdutoFinanceiro p
            where p.ativo = true
              and (:tipo is null or p.tipoProduto = :tipo)
              and (:nivelRisco is null or p.nivelRisco = cast(:nivelRisco as string))
              and (:liquidez is null or p.liquidez = :liquidez)
              and (:nome is null or lower(p.nome) like lower(concat('%', cast(:nome as string), '%')))
            """)
    Page<ProdutoFinanceiro> buscarAtivosFiltrados(
            @Param("tipo") TipoProduto tipo,
            @Param("nivelRisco") String nivelRisco,
            @Param("liquidez") TipoLiquidez liquidez,
            @Param("nome") String nome,
            Pageable pageable);

    @Query("""
            select p
            from ProdutoFinanceiro p
            where (:tipo is null or p.tipoProduto = :tipo)
              and (:nivelRisco is null or p.nivelRisco = cast(:nivelRisco as string))
              and (:liquidez is null or p.liquidez = :liquidez)
              and (:nome is null or lower(p.nome) like lower(concat('%', cast(:nome as string), '%')))
              and (:ativo is null or p.ativo = :ativo)
            """)
    Page<ProdutoFinanceiro> buscarAdminFiltrados(
            @Param("tipo") TipoProduto tipo,
            @Param("nivelRisco") String nivelRisco,
            @Param("liquidez") TipoLiquidez liquidez,
            @Param("nome") String nome,
            @Param("ativo") Boolean ativo,
            Pageable pageable);
}
