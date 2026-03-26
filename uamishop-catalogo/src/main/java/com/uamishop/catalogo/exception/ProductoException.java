package com.uamishop.catalogo.exception;

import com.uamishop.catalogo.shared.exception.DomainException;

public class ProductoException extends DomainException {
    public ProductoException(String message) { super(message); }
    public ProductoException(String message, Throwable cause) { super(message, cause); }
}