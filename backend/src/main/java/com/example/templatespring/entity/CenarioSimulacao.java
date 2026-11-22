package com.example.templatespring.entity;

import com.example.templatespring.entity.enums.TipoProduto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cenario_simulacao")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class CenarioSimulacao {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "simulacao_id", nullable = false)
    private Simulacao simulacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_financeiro_id", nullable = false)
    private ProdutoFinanceiro produtoFinanceiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_produto", nullable = false, length = 30)
    private TipoProduto tipoProduto;

    @Column(name = "valor_inicial", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorInicial;

    @Column(name = "aporte_mensal", nullable = false, precision = 19, scale = 2)
    private BigDecimal aporteMensal;

    @Column(name = "prazo_meses", nullable = false)
    private Integer prazoMeses;

    @Column(name = "valor_final_bruto", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorFinalBruto;

    @Column(name = "valor_final_liquido", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorFinalLiquido;

    @Column(name = "rentabilidade_percentual", nullable = false, precision = 19, scale = 2)
    private BigDecimal rentabilidadePercentual;

    @Column(name = "evolucao_mensal", columnDefinition = "text", nullable = false)
    private String evolucaoMensal;

    @Column(name = "ranking_posicao", nullable = false)
    private Integer rankingPosicao;

    @Column(length = 80)
    private String destaque;
}
