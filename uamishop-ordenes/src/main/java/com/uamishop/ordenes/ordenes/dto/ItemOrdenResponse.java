package com.uamishop.ordenes.ordenes.dto;

import com.uamishop.ordenes.shared.domain.ProductoId;
import com.uamishop.ordenes.ordenes.domain.ItemOrden;
import com.uamishop.ordenes.ordenes.domain.ItemOrdenId;
import com.uamishop.ordenes.shared.domain.Money;

public class ItemOrdenResponse {
    private ItemOrdenId id;
    private ProductoId productoId;
    private String nombreProducto;
    private String sku;
    private int cantidad;
    private Money precioUnitario;
    private Money subtotal;

    public static ItemOrdenResponse fromItemOrden(ItemOrden item) {
        ItemOrdenResponse response = new ItemOrdenResponse();
        response.id = item.getId();
        response.productoId = item.getProductoId();
        response.nombreProducto = item.getNombreProducto();
        response.sku = item.getSku();
        response.cantidad = item.getCantidad();
        response.precioUnitario = item.getPrecioUnitario();
        response.subtotal = item.getSubtotal();
        return response;
    }

    // Getters y setters
    public ItemOrdenId getId() { return id; }
    public void setId(ItemOrdenId id) { this.id = id; }
    public ProductoId getProductoId() { return productoId; }
    public void setProductoId(ProductoId productoId) { this.productoId = productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public Money getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Money precioUnitario) { this.precioUnitario = precioUnitario; }
    public Money getSubtotal() { return subtotal; }
    public void setSubtotal(Money subtotal) { this.subtotal = subtotal; }
}