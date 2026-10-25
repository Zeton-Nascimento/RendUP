package com.example.templatespring.dto.usuario.request;

import com.example.templatespring.entity.enums.PerfilInvestidor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarUsuarioRequest {

    @NotBlank
    @Size(min = 2, max = 120)
    private String nome;

    @NotBlank
    @Pattern(regexp = "^\\d{10,11}$", message = "telefone deve conter DDD + numero (10 ou 11 digitos)")
    private String telefone;

    @NotNull
    @Past(message = "dataNascimento deve ser uma data passada")
    private LocalDate dataNascimento;

    @NotNull
    private PerfilInvestidor perfilInvestidor;
}
