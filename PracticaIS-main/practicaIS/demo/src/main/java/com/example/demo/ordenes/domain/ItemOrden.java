package com.example.demo.ordenes.domain;

import com.example.demo.catalogo.domain.ProductoId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Representa una línea de la orden con información histórica del producto al
 * momento de la compra.
 * El precio es inmutable para preservar la integridad del registro de venta.
 */
@Entity
@Table(name = "items_orden")
public class ItemOrden {

    @EmbeddedId
    private ItemOrdenId id;

    @Embedded
    private ProductoId productoId;

    private String nombreProducto;
    private String sku;
    private Integer cantidad;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cantidad", column = @Column(name = "precio_unitario_cantidad")),
            @AttributeOverride(name = "moneda", column = @Column(name = "precio_unitario_moneda"))
    })
    private Money precioUnitario;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cantidad", column = @Column(name = "subtotal_cantidad")),
            @AttributeOverride(name = "moneda", column = @Column(name = "subtotal_moneda"))
    })
    private Money subtotal;

    /**
     * Constructor protegido para frameworks de persistencia.
     */
    protected ItemOrden() {
    }

    /**
     * Crea una nueva línea de orden capturando el estado del producto en ese
     * momento.
     * 
     * @param id             Identificador de este item.
     * @param productoId     ID del producto original.
     * @param nombreProducto Nombre del producto al momento de la compra.
     * @param sku            SKU del producto.
     * @param cantidad       Cantidad comprada.
     * @param precioUnitario Precio del producto al momento de la compra.
     * @throws IllegalArgumentException si los datos son inválidos o la cantidad no
     *                                  es positiva.
     */
    public ItemOrden(ItemOrdenId id, ProductoId productoId, String nombreProducto,
            String sku, Integer cantidad, Money precioUnitario) {

        if (id == null)
            throw new IllegalArgumentException("El ID del item no puede ser nulo");
        if (productoId == null)
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        if (nombreProducto == null || nombreProducto.isBlank())
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        if (cantidad == null || cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        if (precioUnitario == null)
            throw new IllegalArgumentException("El precio unitario no puede ser nulo");

        this.id = id;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.sku = sku;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = calcularSubtotal();
    }

    /**
     * Calcula el subtotal multiplicando el precio unitario por la cantidad.
     * 
     * @return El subtotal resultante.
     */
    public final Money calcularSubtotal() {
        return precioUnitario.multiplicar(cantidad);
    }

    // Getters
    public ItemOrdenId getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getSku() {
        return sku;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public Money getPrecioUnitario() {
        return precioUnitario;
    }

    public Money getSubtotal() {
        return subtotal;
    }

    @Override
    public String toString() {
        return "ItemOrden{" +
                "id=" + id +
                ", producto='" + nombreProducto + '\'' +
                ", cantidad=" + cantidad +
                ", subtotal=" + subtotal +
                '}';
    }
}
