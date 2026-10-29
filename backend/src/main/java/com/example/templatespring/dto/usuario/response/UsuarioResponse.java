package com.example.templatespring.dto.usuario.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioResponse {

    private UUID id;

    private String nome;

    private String email;

    private boolean ativo;

    private String perfilUsuario;

    private String perfilInvestidor;

    private String cpf;

    private String telefone;

    private LocalDate dataNascimento;

    private String banco;

    private String agencia;

    private String numeroConta;

    private List<String> permissoes;
}
