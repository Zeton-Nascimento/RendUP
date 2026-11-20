package com.example.templatespring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "cenario_referencia_regra")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class CenarioReferenciaRegra {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_regra_id", nullable = false)
    private RegraFinanceiraEvento eventoRegra;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private ProdutoFinanceiro produto;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "valor_entrada", precision = 19, scale = 6)
    private BigDecimal valorEntrada;

    @Column(name = "aporte_mensal", precision = 19, scale = 6)
    private BigDecimal aporteMensal;

    @Column(name = "prazo_dias")
    private Integer prazoDias;

    @Column(name = "taxa_percentual", precision = 12, scale = 6)
    private BigDecimal taxaPercentual;

    @Column(name = "valor_bruto_esperado", precision = 19, scale = 6)
    private BigDecimal valorBrutoEsperado;

    @Column(name = "imposto_esperado", precision = 19, scale = 6)
    private BigDecimal impostoEsperado;

    @Column(name = "valor_liquido_esperado", precision = 19, scale = 6)
    private BigDecimal valorLiquidoEsperado;

    @Column(name = "valor_arredondado_esperado", precision = 19, scale = 6)
    private BigDecimal valorArredondadoEsperado;

    @Column(name = "casas_decimais")
    private Integer casasDecimais;

    @Column(length = 500)
    private String observacoes;
}
