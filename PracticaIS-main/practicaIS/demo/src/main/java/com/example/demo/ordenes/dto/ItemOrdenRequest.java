package com.example.demo.ordenes.dto;
import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.domain.Money;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ItemOrdenRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private ProductoId productoId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombreProducto;

    @NotBlank(message = "El SKU es obligatorio")
    @Pattern(regexp = "[A-Z]{3}-\\d{3}", message = "El SKU debe tener formato AAA-000")
    private String sku;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    private Money precioUnitario;

    // Constructor por defecto necesario para Jackson
    public ItemOrdenRequest() {}

    @JsonCreator
    public ItemOrdenRequest(
            @JsonProperty("productoId") ProductoId productoId,
            @JsonProperty("nombreProducto") String nombreProducto,
            @JsonProperty("sku") String sku,
            @JsonProperty("cantidad") int cantidad,
            @JsonProperty("precioUnitario") Money precioUnitario) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.sku = sku;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters y setters
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
}