package com.example.demo.ventas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AgregarProductoRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private UUID productoId;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;

    public UUID getProductoId() {
        return productoId;
    }

    public void setProductoId(UUID productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}