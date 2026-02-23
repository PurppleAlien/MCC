package com.example.demo.catalogo.exception;

import com.example.demo.shared.exception.DomainException;

public class ProductoException extends DomainException {
    public ProductoException(String message) { super(message); }
    public ProductoException(String message, Throwable cause) { super(message, cause); }
}