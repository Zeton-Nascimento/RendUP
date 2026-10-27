package com.example.templatespring.controller;

import com.example.templatespring.dto.usuario.request.AtualizarUsuarioRequest;
import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.service.AtualizarUsuarioService;
import com.example.templatespring.service.BuscarUsuarioService;
import com.example.templatespring.service.InativarUsuarioService;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final BuscarUsuarioService buscarUsuarioService;
    private final AtualizarUsuarioService atualizarUsuarioService;
    private final InativarUsuarioService inativarUsuarioService;

    public UsuarioController(
            BuscarUsuarioService buscarUsuarioService,
            AtualizarUsuarioService atualizarUsuarioService,
            InativarUsuarioService inativarUsuarioService) {
        this.buscarUsuarioService = buscarUsuarioService;
        this.atualizarUsuarioService = atualizarUsuarioService;
        this.inativarUsuarioService = inativarUsuarioService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_INVESTIDOR', 'ROLE_ADMIN')")
    @GetMapping("/me")
    public UsuarioResponse buscar() {
        return buscarUsuarioService.buscar();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return buscarUsuarioService.listarTodos();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_INVESTIDOR', 'ROLE_ADMIN')")
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

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.OK)
    public UsuarioResponse inativar(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean confirmar) {
        return inativarUsuarioService.inativar(id, confirmar);
    }
}
