package com.example.templatespring.dto.usuario.request;

import com.example.templatespring.entity.enums.PerfilInvestidor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Getter
@Setter
public class RegistroRequest {

  @NotBlank
  @Size(min = 2, max = 120)
  private String nome;

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Pattern(
          regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
          message = "senha deve ter no mínimo 8 caracteres e conter ao menos 1 letra e 1 número"
  )
  private String senha;

  @NotBlank
  @Pattern(regexp = "^\\d{11}$", message = "cpf deve conter 11 digitos numericos")
  private String cpf;

  @NotBlank
  @Pattern(regexp = "^\\d{10,11}$", message = "telefone deve conter DDD + numero (10 ou 11 digitos)")
  private String telefone;

  @NotNull
  @Past(message = "dataNascimento deve ser uma data passada")
  private LocalDate dataNascimento;

  @NotBlank
  @Pattern(regexp = "^[A-Za-z0-9 .'-]{3,100}$", message = "banco deve ter entre 3 e 100 caracteres validos")
  private String banco;

  @NotBlank
  @Pattern(regexp = "^\\d{4,6}$", message = "agencia deve conter entre 4 e 6 digitos")
  private String agencia;

  @NotBlank
  @Pattern(regexp = "^\\d{5,12}$", message = "numeroConta deve conter entre 5 e 12 digitos")
  private String numeroConta;

  @NotNull
  private PerfilInvestidor perfilInvestidor;
}

