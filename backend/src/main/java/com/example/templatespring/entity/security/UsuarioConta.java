package com.example.templatespring.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "usuario_conta")
@Getter
@Setter
@EqualsAndHashCode(of = "usuarioId")
@ToString(of = "usuarioId")
public class UsuarioConta {

    @Id
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @MapsId
    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String banco;

    @Column(nullable = false, length = 10)
    private String agencia;

    @Column(name = "numero_conta", nullable = false, length = 20)
    private String numeroConta;
}
