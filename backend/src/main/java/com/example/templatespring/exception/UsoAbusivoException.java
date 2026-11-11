package com.example.templatespring.exception;

import lombok.Getter;

@Getter
public class UsoAbusivoException extends RuntimeException {

    private final long retryAfterSeconds;

    public UsoAbusivoException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
