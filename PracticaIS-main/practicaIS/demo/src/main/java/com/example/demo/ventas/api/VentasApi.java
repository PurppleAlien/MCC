package com.example.demo.ventas.api;

import com.example.demo.shared.exception.RecursoNoEncontradoException;
import java.util.UUID;

public interface VentasApi {
    CarritoResumen obtenerCarrito(UUID carritoId) throws RecursoNoEncontradoException;
    void completarCheckout(UUID carritoId) throws RecursoNoEncontradoException, IllegalStateException;
}