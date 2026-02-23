package com.example.demo.ventas.domain;

import com.example.demo.catalogo.domain.ProductoId;
import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carritos")
public class Carrito {

    @EmbeddedId
    private CarritoId id;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "cliente_id"))
    private ClienteId clienteId;  // Nuevo campo

    @Enumerated(EnumType.STRING)
    private EstadoCarrito estado;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "carrito_id")
    private List<ItemCarrito> items = new ArrayList<>();

    protected Carrito() {}

    public Carrito(CarritoId id, ClienteId clienteId) {
        if (id == null) throw new IllegalArgumentException("El ID del carrito no puede ser nulo");
        if (clienteId == null) throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        this.id = id;
        this.clienteId = clienteId;
        this.estado = EstadoCarrito.ACTIVO;
    }

    public void agregarProducto(ProductoRef productoRef, int cantidad) {
        validarActivo();
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva");
        if (productoRef == null) throw new IllegalArgumentException("La referencia del producto no puede ser nula");

        Optional<ItemCarrito> existente = buscarItem(productoRef.getProductoId());
        if (existente.isPresent()) {
            existente.get().incrementarCantidad(cantidad);
        } else {
            items.add(new ItemCarrito(
                ItemCarritoId.generar(),
                productoRef.getProductoId(),
                cantidad,
                productoRef.getPrecioUnitario()
            ));
        }
    }

    public void modificarCantidad(ProductoId productoId, int nuevaCantidad) {
        validarActivo();
        ItemCarrito item = buscarItem(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado en el carrito"));
        item.actualizarCantidad(nuevaCantidad);
    }

    public void eliminarProducto(ProductoId productoId) {
        validarActivo();
        items.removeIf(item -> item.getProductoId().equals(productoId));
    }

    public void vaciar() {
        validarActivo();
        items.clear();
    }

    public Money total() {
        return items.stream()
                .map(ItemCarrito::subtotal)
                .reduce(Money.pesos(0), Money::sumar);
    }

    public void iniciarCheckout() {
        if (estado != EstadoCarrito.ACTIVO) {
            throw new IllegalStateException("Solo se puede iniciar checkout en carrito activo");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("No se puede iniciar checkout con carrito vacío");
        }
        if (total().getCantidad().doubleValue() <= 0) {
            throw new IllegalStateException("El total del carrito debe ser mayor a cero");
        }
        this.estado = EstadoCarrito.EN_CHECKOUT;
    }

    public void completarCheckout() {
        if (estado != EstadoCarrito.EN_CHECKOUT) {
            throw new IllegalStateException("Solo se puede completar checkout si el carrito está en checkout");
        }
        this.estado = EstadoCarrito.COMPLETADO;
    }

    public void abandonar() {
        if (estado != EstadoCarrito.EN_CHECKOUT) {
            throw new IllegalStateException("Solo se puede abandonar un carrito en checkout");
        }
        this.estado = EstadoCarrito.ABANDONADO;
    }

    private void validarActivo() {
        if (estado != EstadoCarrito.ACTIVO) {
            throw new IllegalStateException("El carrito no está activo");
        }
    }

    private Optional<ItemCarrito> buscarItem(ProductoId productoId) {
        return items.stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();
    }

    // Getters
    public CarritoId getId() { return id; }
    public ClienteId getClienteId() { return clienteId; }  // Nuevo getter
    public EstadoCarrito getEstado() { return estado; }
    public List<ItemCarrito> getItems() { return items; }
}