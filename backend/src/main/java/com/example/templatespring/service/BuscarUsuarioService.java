package com.example.templatespring.service;

import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.mapper.UsuarioMapper;
import com.example.templatespring.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuscarUsuarioService {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioRepository usuarioRepository;

    public BuscarUsuarioService(
            UsuarioAutenticadoService usuarioAutenticadoService,
            UsuarioRepository usuarioRepository) {
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse buscar() {
        Usuario usuarioAutenticado = usuarioAutenticadoService.get();
        return UsuarioMapper.toResponse(usuarioAutenticado);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailComCargosEPermissoes(email);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toResponse)
                .toList();
    }
}
