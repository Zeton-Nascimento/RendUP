package com.example.templatespring.controller;

import com.example.templatespring.dto.ia.ChatRequest;
import com.example.templatespring.dto.ia.ChatResponse;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.service.UsuarioAutenticadoService;
import com.example.templatespring.service.ia.ChatIAService;
import com.example.templatespring.service.ia.ChatIaRateLimitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat/ia")
public class ChatIAController {

    private final ChatIAService chatIAService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ChatIaRateLimitService rateLimitService;

    public ChatIAController(
            ChatIAService chatIAService,
            UsuarioAutenticadoService usuarioAutenticadoService,
            ChatIaRateLimitService rateLimitService) {
        this.chatIAService = chatIAService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> conversar(@Valid @RequestBody ChatRequest request) {
        Usuario usuario = usuarioAutenticadoService.get();
        if (rateLimitService.estaLimitado(usuario.getId())) {
            ChatResponse resposta = ChatResponse.builder()
                    .resposta("Aguarde alguns segundos antes de enviar outra mensagem.")
                    .fallback(true)
                    .build();
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(resposta);
        }
        return ResponseEntity.ok(chatIAService.processar(request));
    }
}
