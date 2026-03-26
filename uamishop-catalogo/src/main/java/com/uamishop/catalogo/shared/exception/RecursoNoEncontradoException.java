package com.uamishop.catalogo.shared.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso por su ID.
 */
public class RecursoNoEncontradoException extends DomainException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String tipo, Object id) {
        super(String.format("%s con id %s no encontrado", tipo, id));
    }
}