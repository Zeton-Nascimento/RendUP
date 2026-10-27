package com.example.templatespring.dto.login.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

  @NotNull
  @Email
  private String email;

  @NotBlank
  private String senha;
}
