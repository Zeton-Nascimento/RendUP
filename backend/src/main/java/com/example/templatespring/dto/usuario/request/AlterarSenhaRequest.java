package com.example.templatespring.dto.usuario.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlterarSenhaRequest {

    @NotBlank
    @Size(max = 72, message = "senhaAtual deve ter no máximo 72 caracteres")
    private String senhaAtual;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "nova senha deve ter no mínimo 8 caracteres e conter ao menos 1 letra e 1 número")
    @Size(max = 72, message = "novaSenha deve ter no máximo 72 caracteres")
    private String novaSenha;
}
