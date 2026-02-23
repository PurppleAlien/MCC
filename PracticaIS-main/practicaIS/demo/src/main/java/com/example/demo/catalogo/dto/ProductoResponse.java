package com.example.demo.catalogo.dto;

import com.example.demo.catalogo.domain.Imagen;
import com.example.demo.catalogo.domain.Producto;
import com.example.demo.catalogo.domain.ProductoId;
import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.shared.domain.Money;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ProductoResponse {
    private ProductoId id;
    private String nombre;
    private String descripcion;
    private Money precio;
    private Integer stock;
    private String sku;  // NUEVO
    private CategoriaId categoriaId;
    private List<String> imagenesUrls;
    private Boolean disponible;
    private LocalDateTime fechaCreacion;

    public static ProductoResponse fromProducto(Producto producto) {
        ProductoResponse response = new ProductoResponse();
        response.id = producto.getId();
        response.nombre = producto.getNombre();
        response.descripcion = producto.getDescripcion();
        response.precio = producto.getPrecio();
        response.stock = producto.getStock();
        response.sku = producto.getSku();  // NUEVO
        response.categoriaId = producto.getCategoriaId();
        if (producto.getImagenes() != null) {
            response.imagenesUrls = producto.getImagenes().stream()
                    .map(Imagen::url)
                    .collect(Collectors.toList());
        }
        response.disponible = producto.getDisponible();
        response.fechaCreacion = producto.getFechaCreacion();
        return response;
    }

    // Getters y setters
    public ProductoId getId() { return id; }
    public void setId(ProductoId id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Money getPrecio() { return precio; }
    public void setPrecio(Money precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public CategoriaId getCategoriaId() { return categoriaId; }
    public void setCategoriaId(CategoriaId categoriaId) { this.categoriaId = categoriaId; }
    public List<String> getImagenesUrls() { return imagenesUrls; }
    public void setImagenesUrls(List<String> imagenesUrls) { this.imagenesUrls = imagenesUrls; }
    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}