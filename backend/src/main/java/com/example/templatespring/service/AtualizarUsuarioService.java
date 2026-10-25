package com.example.templatespring.service;

import com.example.templatespring.dto.usuario.request.AtualizarUsuarioRequest;
import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.entity.security.UsuarioDadosPessoais;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.mapper.UsuarioMapper;
import com.example.templatespring.repository.UsuarioRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional
    public UsuarioResponse atualizarProprioPerfil(AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioAutenticadoService.get();
        return atualizar(usuario, request);
    }

    @Transactional
    public UsuarioResponse atualizarPorAdministrador(UUID usuarioId, AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        return atualizar(usuario, request);
    }

    private UsuarioResponse atualizar(Usuario usuario, AtualizarUsuarioRequest request) {
        usuario.setNome(request.getNome().trim());
        usuario.setPerfilInvestidor(request.getPerfilInvestidor());

        UsuarioDadosPessoais dadosPessoais = usuario.getDadosPessoais();
        if (dadosPessoais == null) {
            dadosPessoais = new UsuarioDadosPessoais();
            usuario.setDadosPessoais(dadosPessoais);
        }

        dadosPessoais.setTelefone(normalizarSomenteDigitos(request.getTelefone()));
        dadosPessoais.setDataNascimento(request.getDataNascimento());

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponse(usuarioSalvo);
    }

    private String normalizarSomenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}
