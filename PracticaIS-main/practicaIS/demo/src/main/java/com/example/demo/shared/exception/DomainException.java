package com.example.demo.shared.exception;

/**
 * Excepción base para todas las excepciones de dominio.
 */
public abstract class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}