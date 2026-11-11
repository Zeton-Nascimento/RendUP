package com.example.templatespring.dto.login.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

  @NotBlank
  @Email
  @Size(max = 254, message = "email deve ter no máximo 254 caracteres")
  private String email;

  @NotBlank
  @Size(min = 4, max = 72, message = "senha deve ter entre 4 e 72 caracteres")
  private String senha;
}
