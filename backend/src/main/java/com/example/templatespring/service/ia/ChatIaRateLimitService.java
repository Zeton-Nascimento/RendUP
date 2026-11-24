package com.example.templatespring.service.ia;

import com.example.templatespring.config.XaiProperties;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ChatIaRateLimitService {

    private final long intervaloMinimoMs;
    private final Map<UUID, Long> ultimaChamadaPorUsuario = new ConcurrentHashMap<>();

    public ChatIaRateLimitService(XaiProperties properties) {
        intervaloMinimoMs = properties.intervaloMinimoMs() == null
                ? 3_000L
                : properties.intervaloMinimoMs();
    }

    public boolean estaLimitado(UUID usuarioId) {
        long agora = System.currentTimeMillis();
        Long ultimaChamada = ultimaChamadaPorUsuario.get(usuarioId);
        if (ultimaChamada != null && agora - ultimaChamada < intervaloMinimoMs) {
            return true;
        }
        ultimaChamadaPorUsuario.put(usuarioId, agora);
        return false;
    }
}
