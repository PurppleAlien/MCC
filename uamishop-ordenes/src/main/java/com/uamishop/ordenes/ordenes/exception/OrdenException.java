package com.uamishop.ordenes.ordenes.exception;

import com.uamishop.ordenes.shared.exception.DomainException;

public class OrdenException extends DomainException {
    public OrdenException(String message) { super(message); }
    public OrdenException(String message, Throwable cause) { super(message, cause); }
}