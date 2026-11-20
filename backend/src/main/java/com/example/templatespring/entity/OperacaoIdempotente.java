package com.example.templatespring.entity;

import com.example.templatespring.entity.enums.StatusOperacaoIdempotencia;
import com.example.templatespring.entity.enums.TipoOperacaoFinanceira;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "operacao_idempotente",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_operacao_idempotente_usuario_chave",
                columnNames = {"usuario_id", "chave_idempotencia"}))
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class OperacaoIdempotente {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "chave_idempotencia", nullable = false, length = 100)
    private String chaveIdempotencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacao", nullable = false, length = 20)
    private TipoOperacaoFinanceira tipoOperacao;

    @Column(name = "comando_hash", nullable = false, length = 64)
    private String comandoHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOperacaoIdempotencia status;

    @Column(name = "resposta_json", columnDefinition = "text")
    private String respostaJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
