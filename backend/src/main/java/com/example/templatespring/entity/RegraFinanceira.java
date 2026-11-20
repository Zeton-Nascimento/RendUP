package com.example.templatespring.entity;

import com.example.templatespring.entity.enums.StatusRegraFinanceira;
import com.example.templatespring.entity.enums.TipoProduto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "regra_financeira",
        uniqueConstraints = @UniqueConstraint(name = "uk_regra_financeira_tipo", columnNames = "tipo_produto"))
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class RegraFinanceira {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_produto", nullable = false, length = 30)
    private TipoProduto tipoProduto;

    @Column(nullable = false, length = 40)
    private String versao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusRegraFinanceira status;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "regraFinanceira", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("evento ASC")
    private List<RegraFinanceiraEvento> eventos = new ArrayList<>();

    public void substituirEventos(List<RegraFinanceiraEvento> novosEventos) {
        eventos.clear();
        if (novosEventos == null) {
            return;
        }
        novosEventos.forEach(this::adicionarEvento);
    }

    public void adicionarEvento(RegraFinanceiraEvento evento) {
        evento.setRegraFinanceira(this);
        eventos.add(evento);
    }
}
