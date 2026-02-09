package com.example.demo.catalogo.domain;
import jakarta.persistence.*;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.math.BigDecimal;

@Entity
public class Producto {

     @Id
    private ProductoId id;
    private String nombre;
    private Money precio;
    private List<Imagen> imagenes;
    private Boolean disponible;
    private LocalDateTime fechaCreacion;


  public Producto(ProductoId id, String nombre, Money precio, String descripcion, CategoriaId categoriaId) {
        if (id == null) throw new IllegalArgumentException("Id obligatorio");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre obligatorio");
        if (precio == null) throw new IllegalArgumentException("Precio obligatorio");

        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.disponible = true;
        this.fechaCreacion = LocalDateTime.now();
    }


    public ProductoId getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Money getPrecio() {
        return precio;
    }

    public List<Imagen> getImagenes() {
        return imagenes;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

   public static Producto crear(
        String nombre,
        String descripcion,
        Money precio,
        CategoriaId categoriaId
) {
    if (nombre.length() < 3 || nombre.length() > 100) {
        throw new IllegalArgumentException("El nombre debe tener entre 3 y 100 caracteres");
    }
    if (descripcion.length() > 500) {
        throw new IllegalArgumentException("La descripción debe tener menos de 500 caracteres");
    }
    if (precio.cantidad().signum() <= 0) {
        throw new IllegalArgumentException("El precio debe ser mayor a 0");
    }

    return new Producto(
            ProductoId.generar(),
            nombre,
            precio,
            descripcion,
            categoriaId
    );
}
 public void CambiarPrecio(Money nuevoPrecio) {
    // regla 1: el precio no puede ser negativo
    if (nuevoPrecio.cantidad().compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("El precio no puede ser negativo");
    }

    // Regla 2: no aumentar más del 50%
    BigDecimal maximoPermitido =
            this.precio.cantidad().multiply(BigDecimal.valueOf(1.5));

    if (nuevoPrecio.cantidad().compareTo(maximoPermitido) > 0) {
        throw new IllegalArgumentException(
            "El precio no puede incrementarse más del 50% en un solo cambio"
        );
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
            "La URL de la imagen debe comenzar con http:// o https://"
        );
    }

    this.imagenes.add(imagen);
 }

public void desactivar() {
    if (this.disponible == null) {
        this.disponible = true; 
    }

    if (!this.disponible) {
        throw new IllegalStateException(
            "El producto ya está desactivado y no puede desactivarse nuevamente"
        );
    }

    this.disponible = false;
}

public void activar() {
    if (this.disponible == null) {
        this.disponible = false; // estado inicial seguro
    }

    if (this.disponible) {
        throw new IllegalStateException("El producto ya está activo");
    }

    if (this.precio == null ||
        this.precio.cantidad().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalStateException(
            "No se puede activar un producto con precio menor o igual a 0"
        );
    }

    if (this.imagenes == null || this.imagenes.isEmpty()) {
        throw new IllegalStateException(
            "No se puede activar un producto sin al menos una imagen"
        );
    }

    this.disponible = true;
}

}