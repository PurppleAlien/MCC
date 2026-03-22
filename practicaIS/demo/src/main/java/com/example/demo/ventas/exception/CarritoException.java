package com.example.demo.ventas.exception;

import com.example.demo.shared.exception.DomainException;

public class CarritoException extends DomainException {
    public CarritoException(String message) { super(message); }
    public CarritoException(String message, Throwable cause) { super(message, cause); }
}