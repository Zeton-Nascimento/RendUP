package com.example.templatespring.exception;

public class CatalogoFonteException extends RuntimeException {

    public CatalogoFonteException(String mensagem) {
        super(mensagem);
    }

    public CatalogoFonteException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
