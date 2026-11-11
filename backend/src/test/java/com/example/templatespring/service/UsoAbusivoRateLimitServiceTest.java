package com.example.templatespring.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.templatespring.exception.UsoAbusivoException;
import org.junit.jupiter.api.Test;

class UsoAbusivoRateLimitServiceTest {

    @Test
    void bloqueiaChamadasRepetidasDoMesmoConsumidorNoMesmoRecurso() {
        UsoAbusivoRateLimitService service = new UsoAbusivoRateLimitService(15_000L);

        service.verificar("catalogo-sincronizacao", "usuario:admin");

        assertThatThrownBy(() -> service.verificar("catalogo-sincronizacao", "usuario:admin"))
                .isInstanceOf(UsoAbusivoException.class)
                .hasMessageContaining("Muitas solicitações")
                .extracting(exception -> ((UsoAbusivoException) exception).getRetryAfterSeconds())
                .isEqualTo(15L);
    }

    @Test
    void permiteConsumidoresOuRecursosDiferentes() {
        UsoAbusivoRateLimitService service = new UsoAbusivoRateLimitService(15_000L);

        service.verificar("catalogo-importacao", "usuario:admin");
        service.verificar("catalogo-sincronizacao", "usuario:admin");
        service.verificar("catalogo-importacao", "usuario:outro-admin");
    }
}
