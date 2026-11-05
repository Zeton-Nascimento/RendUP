package com.example.templatespring.entity;

import com.example.templatespring.entity.enums.TipoLiquidez;
import com.example.templatespring.entity.enums.TipoProduto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "produto_financeiro")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class ProdutoFinanceiro {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_produto", nullable = false)
    private TipoProduto tipoProduto;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "rentabilidade_percentual", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentabilidadePercentual;

    @Column(name = "valor_minimo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMinimo;

    @Column(name = "nivel_risco", nullable = false, length = 10)
    private String nivelRisco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLiquidez liquidez;

    @Column(name = "dias_liquidez")
    private Integer diasLiquidez;

    @Column(name = "prazo_dias", nullable = false)
    private Integer prazoDias;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
