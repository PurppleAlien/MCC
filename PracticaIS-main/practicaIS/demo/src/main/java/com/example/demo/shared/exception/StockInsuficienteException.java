package com.example.demo.shared.exception;

/**
 * Excepción lanzada cuando no hay suficiente stock de un producto.
 */
public class StockInsuficienteException extends DomainException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    public StockInsuficienteException(String producto, int solicitado, int disponible) {
        super(String.format("Stock insuficiente para el producto %s. Solicitado: %d, disponible: %d",
                producto, solicitado, disponible));
    }
}