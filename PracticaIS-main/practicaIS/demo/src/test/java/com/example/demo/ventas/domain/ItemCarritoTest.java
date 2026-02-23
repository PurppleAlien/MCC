package com.example.demo.ventas.domain;

import com.example.demo.catalogo.domain.ProductoId;
import com.example.demo.shared.domain.Money;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ItemCarritoTest {

    @Test
    void deberiaCrearItemCorrectamente() {
        ItemCarritoId id = ItemCarritoId.generar();
        ProductoId prodId = new ProductoId(UUID.randomUUID());
        Money precio = Money.pesos(150);
        ItemCarrito item = new ItemCarrito(id, prodId, 3, precio);
        assertNotNull(item);
        assertEquals(id, item.getId());
        assertEquals(prodId, item.getProductoId());
        assertEquals(3, item.getCantidad());
        assertEquals(precio, item.getPrecioUnitario());
    }

    @Test
    void deberiaCalcularSubtotal() {
        ItemCarrito item = new ItemCarrito(
            ItemCarritoId.generar(),
            new ProductoId(UUID.randomUUID()),
            4,
            Money.pesos(75)
        );
        assertEquals(Money.pesos(300), item.subtotal());
    }

    @Test
    void deberiaIncrementarCantidad() {
        ItemCarrito item = new ItemCarrito(
            ItemCarritoId.generar(),
            new ProductoId(UUID.randomUUID()),
            2,
            Money.pesos(100)
        );
        item.incrementarCantidad(3);
        assertEquals(5, item.getCantidad());
        assertEquals(Money.pesos(500), item.subtotal());
    }

    @Test
    void deberiaActualizarCantidad() {
        ItemCarrito item = new ItemCarrito(
            ItemCarritoId.generar(),
            new ProductoId(UUID.randomUUID()),
            2,
            Money.pesos(100)
        );
        item.actualizarCantidad(5);
        assertEquals(5, item.getCantidad());
    }

    @Test
    void noDebeAceptarCantidadCero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ItemCarrito(
                ItemCarritoId.generar(),
                new ProductoId(UUID.randomUUID()),
                0,
                Money.pesos(100)
            );
        });
    }
}