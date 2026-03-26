package com.example.demo.catalogo.domain;

import jakarta.persistence.*;

@Entity
public class Categoria {
    @EmbeddedId
    private CategoriaId id;

    private String nombre;
    private String descripcion;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "categoria_padre_id"))
    private CategoriaId categoriaPadreId;

    protected Categoria() {
    }

    public Categoria(CategoriaId id, String nombre, String descripcion, CategoriaId categoriaPadreId) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede ser nulo o vacío");
        }
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoriaPadreId = categoriaPadreId;
    }

    public void actualizar(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede ser nulo o vacío");
        }
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void AsignarPadre(CategoriaId categoriaPadreId) {
        if (categoriaPadreId == null) {
            throw new IllegalArgumentException("El ID de la categoría padre no puede ser nulo");
        }
        this.categoriaPadreId = categoriaPadreId;
    }

    public CategoriaId getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public CategoriaId getCategoriaPadreId() {
        return categoriaPadreId;
    }
}