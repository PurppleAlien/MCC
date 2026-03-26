package com.example.demo.catalogo.service;

import com.example.demo.catalogo.api.CatalogoApi;
import com.example.demo.catalogo.api.ProductoResumen;
import com.example.demo.catalogo.domain.Producto;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.shared.exception.StockInsuficienteException;
import org.springframework.stereotype.Service;

@Service
public class CatalogoServiceImpl implements CatalogoApi {

    private final ProductoJpaRepository productoRepository;

    public CatalogoServiceImpl(ProductoJpaRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public ProductoResumen obtenerProducto(ProductoId id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id.getValue()));
        return new ProductoResumen(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getSku()
        );
    }

    @Override
    public void validarStock(ProductoId id, int cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id.getValue()));
        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(producto.getNombre(), cantidad, producto.getStock());
        }
    }
}