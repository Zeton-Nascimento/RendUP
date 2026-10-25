package com.example.templatespring.entity.security;

import com.example.templatespring.entity.enums.PerfilInvestidor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_investidor", nullable = false)
    private PerfilInvestidor perfilInvestidor = PerfilInvestidor.CONSERVADOR;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "saldo_disponivel", nullable = false)
    private BigDecimal saldoDisponivel = new BigDecimal("0.00");

    @ManyToMany(fetch = EAGER)
    @JoinTable(
            name = "usuario_cargo",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "cargo_id")
    )
    private Set<Cargo> cargos = new LinkedHashSet<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private UsuarioDadosPessoais dadosPessoais;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private UsuarioConta conta;

    public String getSenha() {
        return senhaHash;
    }

    public void setSenha(String senha) {
        this.senhaHash = senha;
    }

    public Set<Permissao> getPermissoes() {
        return cargos.stream()
                .flatMap(cargo -> cargo.getPermissoes().stream())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public void adicionarCargo(Cargo cargo) {
        this.cargos.add(cargo);
    }

    public void setDadosPessoais(UsuarioDadosPessoais dadosPessoais) {
        if (dadosPessoais == null) {
            if (this.dadosPessoais != null) {
                this.dadosPessoais.setUsuario(null);
            }
            this.dadosPessoais = null;
            return;
        }

        dadosPessoais.setUsuario(this);
        this.dadosPessoais = dadosPessoais;
    }

    public void setConta(UsuarioConta conta) {
        if (conta == null) {
            if (this.conta != null) {
                this.conta.setUsuario(null);
            }
            this.conta = null;
            return;
        }

        conta.setUsuario(this);
        this.conta = conta;
    }
}
