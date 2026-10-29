package com.example.templatespring.service;

import com.example.templatespring.dto.usuario.request.AlterarSenhaRequest;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlterarSenhaService {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void alterar(AlterarSenhaRequest request) {
        Usuario usuario = usuarioAutenticadoService.get();

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
            throw new BadCredentialsException("Senha atual incorreta.");
        }

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }
}
