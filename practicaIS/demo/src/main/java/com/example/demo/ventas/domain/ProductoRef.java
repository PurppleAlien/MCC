package com.example.demo.ventas.domain;

import com.example.demo.shared.domain.ProductoId; // <-- IMPORTACIÓN AGREGADA
import com.example.demo.shared.domain.Money;

public class ProductoRef {
    private final ProductoId productoId;
    private final Money precioUnitario;

    public ProductoRef(ProductoId productoId, Money precioUnitario) {
        if (productoId == null) throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        if (precioUnitario == null) throw new IllegalArgumentException("El precio unitario no puede ser nulo");
        this.productoId = productoId;
        this.precioUnitario = precioUnitario;
    }

    public ProductoId getProductoId() { return productoId; }
    public Money getPrecioUnitario() { return precioUnitario; }
}