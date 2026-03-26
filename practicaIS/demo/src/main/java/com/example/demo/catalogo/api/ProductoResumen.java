package com.example.demo.catalogo.api;

import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductoResumen(
    @JsonProperty("id") ProductoId id,
    @JsonProperty("nombre") String nombre,
    @JsonProperty("precio") Money precio,
    @JsonProperty("stock") Integer stock,
    @JsonProperty("sku") String sku
) {
    @JsonCreator
    public ProductoResumen {
        // Puedes agregar validaciones si lo deseas
    }
}