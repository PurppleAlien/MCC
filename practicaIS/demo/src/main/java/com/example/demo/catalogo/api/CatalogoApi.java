package com.example.demo.catalogo.api;

import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.shared.exception.StockInsuficienteException;

public interface CatalogoApi {
    ProductoResumen obtenerProducto(ProductoId id) throws RecursoNoEncontradoException;
    void validarStock(ProductoId id, int cantidad) throws StockInsuficienteException, RecursoNoEncontradoException;
}