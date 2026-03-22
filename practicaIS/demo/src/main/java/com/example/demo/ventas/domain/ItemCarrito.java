package com.example.demo.ventas.domain;

import com.example.demo.shared.domain.ProductoId; // <-- IMPORTACIÓN AGREGADA
import com.example.demo.shared.domain.Money;
import jakarta.persistence.*;

@Entity
@Table(name = "items_carrito")
public class ItemCarrito {

    @EmbeddedId
    private ItemCarritoId id;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "producto_id"))
    private ProductoId productoId;

    private int cantidad;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "cantidad", column = @Column(name = "precio_unitario_cantidad")),
        @AttributeOverride(name = "moneda", column = @Column(name = "precio_unitario_moneda"))
    })
    private Money precioUnitario;

    protected ItemCarrito() {}

    public ItemCarrito(ItemCarritoId id, ProductoId productoId, int cantidad, Money precioUnitario) {
        if (id == null) throw new IllegalArgumentException("El ID del item no puede ser nulo");
        if (productoId == null) throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva");
        if (precioUnitario == null) throw new IllegalArgumentException("El precio unitario no puede ser nulo");

        this.id = id;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Money subtotal() {
        return precioUnitario.multiplicar(cantidad);
    }

    public void incrementarCantidad(int adicional) {
        if (adicional <= 0) throw new IllegalArgumentException("La cantidad a incrementar debe ser positiva");
        this.cantidad += adicional;
    }

    public void disminuirCantidad(int sustraendo) {
        if (sustraendo <= 0) throw new IllegalArgumentException("La cantidad a disminuir debe ser positiva");
        if (this.cantidad - sustraendo < 1) throw new IllegalArgumentException("El item no puede quedar con cantidad cero o negativa");
        this.cantidad -= sustraendo;
    }

    public void actualizarCantidad(int nuevaCantidad) {
        if (nuevaCantidad <= 0) throw new IllegalArgumentException("La nueva cantidad debe ser positiva");
        this.cantidad = nuevaCantidad;
    }

    // Getters
    public ItemCarritoId getId() { return id; }
    public ProductoId getProductoId() { return productoId; }
    public int getCantidad() { return cantidad; }
    public Money getPrecioUnitario() { return precioUnitario; }
}