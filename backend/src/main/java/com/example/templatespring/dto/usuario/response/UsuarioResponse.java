package com.example.templatespring.dto.usuario.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsuarioResponse {

    private UUID id;

    private String nome;

    private String email;

    private String perfilInvestidor;

    private String cpf;

    private String telefone;

    private LocalDate dataNascimento;

    private String banco;

    private String agencia;

    private String numeroConta;

    private List<String> permissoes;
}
