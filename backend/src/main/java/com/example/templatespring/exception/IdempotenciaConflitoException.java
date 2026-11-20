package com.example.templatespring.exception;

public class IdempotenciaConflitoException extends RuntimeException {

    public IdempotenciaConflitoException(String message) {
        super(message);
    }
}
