package com.example.templatespring.dto.usuario.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlterarSenhaRequest {

    @NotBlank
    private String senhaAtual;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "nova senha deve ter no mínimo 8 caracteres e conter ao menos 1 letra e 1 número")
    private String novaSenha;
}
