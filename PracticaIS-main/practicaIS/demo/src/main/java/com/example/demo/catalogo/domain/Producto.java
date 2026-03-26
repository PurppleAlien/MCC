package com.example.demo.catalogo.domain;


import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId; // <-- IMPORTACIÓN AGREGADA
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;


@Entity
public class Producto {

    @EmbeddedId
    private ProductoId id;

    private String nombre;
    private String descripcion;

    @Embedded
    private Money precio;

    private Integer stock;

    private String sku;  // NUEVO CAMPO

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "categoria_id"))
    private CategoriaId categoriaId;

    @ElementCollection
    @CollectionTable(name = "producto_imagenes", joinColumns = @JoinColumn(name = "producto_id"))
    private List<Imagen> imagenes;

    private Boolean disponible;
    private LocalDateTime fechaCreacion;

    protected Producto() {
    }

    // Constructor actualizado con sku
    public Producto(ProductoId id, String nombre, Money precio, Integer stock, String sku, String descripcion, CategoriaId categoriaId) {
        if (id == null)
            throw new IllegalArgumentException("Id obligatorio");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre obligatorio");
        if (precio == null)
            throw new IllegalArgumentException("Precio obligatorio");
        if (stock == null || stock < 0)
            throw new IllegalArgumentException("Stock debe ser mayor o igual a 0");
        if (sku == null || sku.isBlank())
            throw new IllegalArgumentException("SKU obligatorio");

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.sku = sku;
        this.categoriaId = categoriaId;
        this.disponible = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters
    public ProductoId getId() { return id; }
    public String getNombre() { return nombre; }
    public Money getPrecio() { return precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public List<Imagen> getImagenes() { return imagenes; }
    public Boolean getDisponible() { return disponible; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public String getDescripcion() { return descripcion; }
    public CategoriaId getCategoriaId() { return categoriaId; }

    public void actualizarNombreDescripcion(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (descripcion != null && descripcion.length() > 500) {
            throw new IllegalArgumentException("La descripción no puede exceder 500 caracteres");
        }
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void asignarCategoria(CategoriaId nuevaCategoriaId) {
        if (nuevaCategoriaId == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }
        this.categoriaId = nuevaCategoriaId;
    }

    public static Producto crear(
            String nombre,
            String descripcion,
            Money precio,
            Integer stock,
            String sku,
            CategoriaId categoriaId) {
        if (nombre.length() < 3 || nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener entre 3 y 100 caracteres");
        }
        if (descripcion.length() > 500) {
            throw new IllegalArgumentException("La descripción debe tener menos de 500 caracteres");
        }
        if (precio.getCantidad().signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("El stock debe ser mayor o igual a 0");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("El SKU es obligatorio");
        }

        return new Producto(
                ProductoId.generar(),
                nombre,
                precio,
                stock,
                sku,
                descripcion,
                categoriaId);
    }

    public void CambiarPrecio(Money nuevoPrecio) {
        if (nuevoPrecio.getCantidad().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        BigDecimal maximoPermitido = this.precio.getCantidad().multiply(BigDecimal.valueOf(1.5));
        if (nuevoPrecio.getCantidad().compareTo(maximoPermitido) > 0) {
            throw new IllegalArgumentException(
                    "El precio no puede incrementarse más del 50% en un solo cambio");
        }
        this.precio = nuevoPrecio;
    }

    public void agregarImagen(Imagen imagen) {
        if (imagen == null) {
            throw new IllegalArgumentException("La imagen no puede ser nula");
        }
        if (this.imagenes == null) {
            this.imagenes = new ArrayList<>();
        }
        if (this.imagenes.size() >= 5) {
            throw new IllegalArgumentException("No se pueden agregar más de 5 imágenes");
        }
        String url = imagen.url();
        if (url == null ||
                !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new IllegalArgumentException(
                    "La URL de la imagen debe comenzar con http:// o https://");
        }
        this.imagenes.add(imagen);
    }

    public void desactivar() {
        if (this.disponible == null) {
            this.disponible = true;
        }
        if (!this.disponible) {
            throw new IllegalStateException(
                    "El producto ya está desactivado y no puede desactivarse nuevamente");
        }
        this.disponible = false;
    }

    public void activar() {
        if (this.disponible == null) {
            this.disponible = false;
        }
        if (this.disponible) {
            throw new IllegalStateException("El producto ya está activo");
        }
        if (this.precio == null ||
                this.precio.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "No se puede activar un producto con precio menor o igual a 0");
        }
        if (this.imagenes == null || this.imagenes.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede activar un producto sin al menos una imagen");
        }
        this.disponible = true;
    }
}