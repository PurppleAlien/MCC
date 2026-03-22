package com.example.demo.catalogo.dto;

import com.example.demo.catalogo.domain.Categoria;
import com.example.demo.catalogo.domain.CategoriaId;

public class CategoriaResponse {
    private CategoriaId id;
    private String nombre;
    private String descripcion;
    private CategoriaId categoriaPadreId;

    public static CategoriaResponse fromCategoria(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.id = categoria.getId();
        response.nombre = categoria.getNombre();
        response.descripcion = categoria.getDescripcion();
        response.categoriaPadreId = categoria.getCategoriaPadreId();
        return response;
    }

    // Getters y setters
    public CategoriaId getId() { return id; }
    public void setId(CategoriaId id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public CategoriaId getCategoriaPadreId() { return categoriaPadreId; }
    public void setCategoriaPadreId(CategoriaId categoriaPadreId) { this.categoriaPadreId = categoriaPadreId; }
}