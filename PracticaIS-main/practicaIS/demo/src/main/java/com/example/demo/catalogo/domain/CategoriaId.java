package com.example.demo.catalogo.domain;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public record CategoriaId(UUID id) {

    public CategoriaId {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }
    }

    public static CategoriaId generar() {
        return new CategoriaId(UUID.randomUUID());
    }

    public static CategoriaId Getvalue(UUID id) {
        return new CategoriaId(id);
    }
}
