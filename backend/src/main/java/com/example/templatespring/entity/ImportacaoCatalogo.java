package com.example.templatespring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "importacao_catalogo")
@Getter
@Setter
public class ImportacaoCatalogo {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "data_importacao", nullable = false)
    private LocalDateTime dataImportacao;

    @Column(name = "total_itens", nullable = false)
    private int totalItens;

    @Column(nullable = false)
    private int inseridos;

    @Column(nullable = false)
    private int atualizados;

    @Column(nullable = false)
    private int rejeitados;

    @Column
    private int inativados;

    @Column(name = "usuario_responsavel", length = 255)
    private String usuarioResponsavel;

    @Column(name = "versao_catalogo", length = 100)
    private String versaoCatalogo;

    @Column(name = "origem_catalogo", length = 100)
    private String origemCatalogo;

    @Column(name = "hash_importacao", length = 64)
    private String hashImportacao;

    @Column(length = 20)
    private String status;

    @Column(columnDefinition = "text")
    private String mensagem;

    @Column(columnDefinition = "text")
    private String erros;
}
