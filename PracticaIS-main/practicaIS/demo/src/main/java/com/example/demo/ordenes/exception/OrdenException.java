package com.example.demo.ordenes.exception;

import com.example.demo.shared.exception.DomainException;

public class OrdenException extends DomainException {
    public OrdenException(String message) { super(message); }
    public OrdenException(String message, Throwable cause) { super(message, cause); }
}