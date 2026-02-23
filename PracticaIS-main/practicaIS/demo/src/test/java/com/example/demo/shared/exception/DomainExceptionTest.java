package com.example.demo.shared.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionTest {

    @Test
    void deberiaCrearRecursoNoEncontradoConMensaje() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Producto no encontrado");
        assertEquals("Producto no encontrado", ex.getMessage());
    }

    @Test
    void deberiaCrearRecursoNoEncontradoConTipoYId() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Producto", "123");
        assertEquals("Producto con id 123 no encontrado", ex.getMessage());
    }

    @Test
    void deberiaCrearStockInsuficienteConMensaje() {
        StockInsuficienteException ex = new StockInsuficienteException("Stock insuficiente");
        assertEquals("Stock insuficiente", ex.getMessage());
    }

    @Test
    void deberiaCrearStockInsuficienteConDetalles() {
        StockInsuficienteException ex = new StockInsuficienteException("Camisa", 5, 2);
        assertEquals("Stock insuficiente para el producto Camisa. Solicitado: 5, disponible: 2", ex.getMessage());
    }
}