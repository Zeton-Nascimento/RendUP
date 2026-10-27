package com.example.templatespring.service;

import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.mapper.UsuarioMapper;
import com.example.templatespring.repository.UsuarioRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class InativarUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional
    public UsuarioResponse inativar(UUID usuarioId, boolean confirmar) {
        if (!confirmar) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A confirmação da inativação é obrigatória.");
        }

        Usuario usuarioAutenticado = usuarioAutenticadoService.get();
        if (usuarioAutenticado.getId().equals(usuarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Um administrador não pode inativar a própria conta.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        if (!usuario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já está inativo.");
        }

        usuario.setAtivo(false);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponse(usuarioSalvo);
    }
}
