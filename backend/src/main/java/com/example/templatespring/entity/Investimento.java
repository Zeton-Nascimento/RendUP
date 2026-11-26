package com.example.templatespring.entity;

import com.example.templatespring.entity.enums.Indexador;
import com.example.templatespring.entity.enums.StatusInvestimento;
import com.example.templatespring.entity.security.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "investimento")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class Investimento {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_financeiro_id", nullable = false)
    private ProdutoFinanceiro produtoFinanceiro;

    @Column(name = "valor_aplicado", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorAplicado;

    @Column(name = "valor_atual", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorAtual;

    @Column(name = "data_aplicacao", nullable = false)
    private LocalDate dataAplicacao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusInvestimento status;

    @Column(name = "taxa_contratada", nullable = false, precision = 10, scale = 6)
    private BigDecimal taxaContratada;

    @Enumerated(EnumType.STRING)
    @Column(name = "indexador_contratado", length = 20)
    private Indexador indexadorContratado;

    @Column(name = "percentual_indexador_contratado", precision = 10, scale = 2)
    private BigDecimal percentualIndexadorContratado;

    @Column(name = "carencia_dias", nullable = false)
    private Integer carenciaDiasContratada;

    @Column(name = "bloqueado", nullable = false)
    private boolean bloqueado;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
