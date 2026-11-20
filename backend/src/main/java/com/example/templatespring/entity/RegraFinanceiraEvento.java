package com.example.templatespring.entity;

import com.example.templatespring.entity.enums.EventoFinanceiro;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "regra_financeira_evento",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_regra_financeira_evento",
                columnNames = {"regra_financeira_id", "evento"}))
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class RegraFinanceiraEvento {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "regra_financeira_id", nullable = false)
    private RegraFinanceira regraFinanceira;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventoFinanceiro evento;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false, length = 1000)
    private String formula;

    @OneToMany(mappedBy = "eventoRegra", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nome ASC")
    private List<CenarioReferenciaRegra> cenariosReferencia = new ArrayList<>();

    public void adicionarCenario(CenarioReferenciaRegra cenario) {
        cenario.setEventoRegra(this);
        cenariosReferencia.add(cenario);
    }
}
