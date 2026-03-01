package com.example.demo.catalogo.api;

import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId;

public record ProductoResumen(
    ProductoId id,
    String nombre,
    Money precio,
    Integer stock,
    String sku
) {}