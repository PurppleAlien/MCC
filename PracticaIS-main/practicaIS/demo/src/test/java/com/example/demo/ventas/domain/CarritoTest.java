package com.example.demo.ventas.domain;

import com.example.demo.catalogo.domain.ProductoId;
import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ClienteId;  // Importar desde shared
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CarritoTest {

    private ClienteId crearClienteId() {
        return new ClienteId(UUID.randomUUID());
    }

    @Test
    void deberiaCrearCarritoActivo() {
        CarritoId id = CarritoId.generar();
        ClienteId clienteId = crearClienteId();
        Carrito carrito = new Carrito(id, clienteId);

        assertNotNull(carrito);
        assertEquals(id, carrito.getId());
        assertEquals(clienteId, carrito.getClienteId());
        assertEquals(EstadoCarrito.ACTIVO, carrito.getEstado());
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void deberiaAgregarProductoNuevo() {
        Carrito carrito = new Carrito(CarritoId.generar(), crearClienteId());
        ProductoId prodId = new ProductoId(UUID.randomUUID());
        Money precio = Money.pesos(100);
        ProductoRef ref = new ProductoRef(prodId, precio);

        carrito.agregarProducto(ref, 2);

        assertEquals(1, carrito.getItems().size());
        ItemCarrito item = carrito.getItems().get(0);
        assertEquals(prodId, item.getProductoId());
        assertEquals(2, item.getCantidad());
        assertEquals(precio, item.getPrecioUnitario());
        assertEquals(Money.pesos(200), item.subtotal());
    }

    @Test
    void deberiaIncrementarCantidadSiProductoYaExiste() {
        Carrito carrito = new Carrito(CarritoId.generar(), crearClienteId());
        ProductoId prodId = new ProductoId(UUID.randomUUID());
        Money precio = Money.pesos(100);
        ProductoRef ref = new ProductoRef(prodId, precio);

        carrito.agregarProducto(ref, 2);
        carrito.agregarProducto(ref, 3);

        assertEquals(1, carrito.getItems().size());
        assertEquals(5, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void deberiaCalcularTotalCorrectamente() {
        Carrito carrito = new Carrito(CarritoId.generar(), crearClienteId());
        ProductoId p1 = new ProductoId(UUID.randomUUID());
        ProductoId p2 = new ProductoId(UUID.randomUUID());

        carrito.agregarProducto(new ProductoRef(p1, Money.pesos(100)), 2);
        carrito.agregarProducto(new ProductoRef(p2, Money.pesos(50)), 1);

        assertEquals(Money.pesos(250), carrito.total());
    }

    @Test
    void deberiaEliminarProducto() {
        Carrito carrito = new Carrito(CarritoId.generar(), crearClienteId());
        ProductoId prodId = new ProductoId(UUID.randomUUID());

        carrito.agregarProducto(new ProductoRef(prodId, Money.pesos(100)), 2);
        assertEquals(1, carrito.getItems().size());

        carrito.eliminarProducto(prodId);
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void deberiaVaciarCarrito() {
        Carrito carrito = new Carrito(CarritoId.generar(), crearClienteId());

        carrito.agregarProducto(new ProductoRef(new ProductoId(UUID.randomUUID()), Money.pesos(100)), 2);
        carrito.agregarProducto(new ProductoRef(new ProductoId(UUID.randomUUID()), Money.pesos(50)), 1);

        assertEquals(2, carrito.getItems().size());

        carrito.vaciar();
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void deberiaIniciarCheckoutCorrectamente() {
        Carrito carrito = new Carrito(CarritoId.generar(), crearClienteId());

        carrito.agregarProducto(new ProductoRef(new ProductoId(UUID.randomUUID()), Money.pesos(100)), 1);
        carrito.iniciarCheckout();

        assertEquals(EstadoCarrito.EN_CHECKOUT, carrito.getEstado());
    }

    @Test
    void noDebeIniciarCheckoutSiCarritoVacio() {
        Carrito carrito = new Carrito(CarritoId.generar(), crearClienteId());

        assertThrows(IllegalStateException.class, carrito::iniciarCheckout);
    }

    @Test
    void noDebeIniciarCheckoutSiTotalEsCero() {
        Carrito carrito = new Carrito(CarritoId.generar(), crearClienteId());
        ProductoId prodId = new ProductoId(UUID.randomUUID());
        Money precioCero = new Money(java.math.BigDecimal.ZERO, "MXN");
        carrito.agregarProducto(new ProductoRef(prodId, precioCero), 1);

        assertThrows(IllegalStateException.class, carrito::iniciarCheckout);
    }
}