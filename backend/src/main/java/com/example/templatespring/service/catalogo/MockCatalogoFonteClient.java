package com.example.templatespring.service.catalogo;

import com.example.templatespring.dto.produto.response.CatalogoFonteResponse;
import com.example.templatespring.exception.CatalogoFonteException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class MockCatalogoFonteClient implements CatalogoFonteClient {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${catalogo.sincronizacao.mock.arquivo:classpath:catalogo/catalogo-mock-v1.json}")
    private String arquivo;

    @Value("${catalogo.sincronizacao.mock.falhar:false}")
    private boolean simularFalha;

    @Value("${catalogo.sincronizacao.mock.atraso-ms:0}")
    private long atrasoMs;

    @Value("${catalogo.sincronizacao.mock.timeout-ms:3000}")
    private long timeoutMs;

    public MockCatalogoFonteClient(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public CatalogoFonteResponse buscarCatalogo() {
        if (simularFalha) {
            throw new CatalogoFonteException("A fonte mockada do catálogo está indisponível.");
        }

        if (atrasoMs > timeoutMs) {
            throw new CatalogoFonteException("A fonte mockada excedeu o tempo limite de resposta.");
        }

        aguardarRespostaMockada();

        Resource resource = resourceLoader.getResource(arquivo);
        if (!resource.exists()) {
            throw new CatalogoFonteException("Arquivo do catálogo mockado não encontrado.");
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, CatalogoFonteResponse.class);
        } catch (IOException exception) {
            throw new CatalogoFonteException("Não foi possível ler o catálogo mockado.", exception);
        }
    }

    private void aguardarRespostaMockada() {
        if (atrasoMs <= 0) {
            return;
        }

        try {
            Thread.sleep(atrasoMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CatalogoFonteException("A leitura do catálogo mockado foi interrompida.", exception);
        }
    }
}
