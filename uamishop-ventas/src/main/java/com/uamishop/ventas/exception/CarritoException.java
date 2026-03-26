package com.uamishop.ventas.exception;

import com.uamishop.ventas.shared.exception.DomainException;

public class CarritoException extends DomainException {
    public CarritoException(String message) { super(message); }
    public CarritoException(String message, Throwable cause) { super(message, cause); }
}