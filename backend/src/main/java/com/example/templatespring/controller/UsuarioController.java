package com.example.templatespring.controller;

import com.example.templatespring.dto.usuario.request.AtualizarUsuarioRequest;
import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.service.AtualizarUsuarioService;
import com.example.templatespring.service.BuscarUsuarioService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final BuscarUsuarioService buscarUsuarioService;
    private final AtualizarUsuarioService atualizarUsuarioService;

    public UsuarioController(
            BuscarUsuarioService buscarUsuarioService,
            AtualizarUsuarioService atualizarUsuarioService) {
        this.buscarUsuarioService = buscarUsuarioService;
        this.atualizarUsuarioService = atualizarUsuarioService;
    }

    @GetMapping("/me")
    public UsuarioResponse buscar() {
        return buscarUsuarioService.buscar();
    }

    @PutMapping("/me")
    public UsuarioResponse atualizarProprioPerfil(@Valid @RequestBody AtualizarUsuarioRequest request) {
        return atualizarUsuarioService.atualizarProprioPerfil(request);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public UsuarioResponse atualizarPorAdministrador(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarUsuarioRequest request) {
        return atualizarUsuarioService.atualizarPorAdministrador(id, request);
    }
}
