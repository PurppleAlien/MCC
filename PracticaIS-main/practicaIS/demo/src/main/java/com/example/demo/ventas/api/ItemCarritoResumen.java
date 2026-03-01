package com.example.demo.ventas.api;

import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId;

public record ItemCarritoResumen(
    ProductoId productoId,
    int cantidad,
    Money precioUnitario
) {}