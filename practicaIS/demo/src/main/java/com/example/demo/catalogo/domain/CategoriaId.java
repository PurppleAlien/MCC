package com.example.demo.catalogo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CategoriaId {

    private final UUID id;

    protected CategoriaId() {
        this.id = null;
    }

    @JsonCreator
    public CategoriaId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }
        this.id = id;
    }

    public static CategoriaId generar() {
        return new CategoriaId(UUID.randomUUID());
    }

    @JsonValue
    public UUID id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoriaId that = (CategoriaId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id != null ? id.toString() : "null";
    }
}