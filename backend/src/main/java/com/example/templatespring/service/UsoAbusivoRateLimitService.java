package com.example.templatespring.service;

import com.example.templatespring.exception.UsoAbusivoException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UsoAbusivoRateLimitService {

    private final long intervaloCatalogoMs;
    private final Map<String, Long> ultimaChamadaPorConsumidor = new ConcurrentHashMap<>();

    public UsoAbusivoRateLimitService(
            @Value("${app.rate-limit.catalogo-intervalo-ms:15000}") long intervaloCatalogoMs) {
        this.intervaloCatalogoMs = Math.max(1_000L, intervaloCatalogoMs);
    }

    public synchronized void verificar(String recurso, String consumidor) {
        String chave = recurso + ":" + consumidor;
        long agora = System.currentTimeMillis();
        Long ultimaChamada = ultimaChamadaPorConsumidor.get(chave);

        if (ultimaChamada != null) {
            long restante = intervaloCatalogoMs - (agora - ultimaChamada);
            if (restante > 0) {
                long retryAfterSeconds = Math.max(1L, (restante + 999L) / 1_000L);
                throw new UsoAbusivoException(
                        "Muitas solicitações para este recurso. Tente novamente em "
                                + retryAfterSeconds + " segundo(s).",
                        retryAfterSeconds);
            }
        }

        ultimaChamadaPorConsumidor.put(chave, agora);
    }
}
