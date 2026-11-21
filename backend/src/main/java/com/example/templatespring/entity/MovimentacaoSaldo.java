package com.example.templatespring.entity;

import com.example.templatespring.entity.enums.TipoMovimentacaoSaldo;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "movimentacao_saldo")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class MovimentacaoSaldo {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacaoSaldo tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_antes", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoAntes;

    @Column(name = "saldo_apos", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoApos;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDateTime dataMovimentacao;
}
