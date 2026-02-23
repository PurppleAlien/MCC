package com.example.demo.ordenes.domain;

import com.example.demo.catalogo.domain.ProductoId;
import com.example.demo.shared.domain.ClienteId; // <-- IMPORTACIÓN AGREGADA
import com.example.demo.shared.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrdenTest {

    @Test
    @DisplayName("Debe crear una orden correctamente con subtotal y total válidos")
    void testCrearOrden() {
        OrdenId idOrden = new OrdenId(UUID.randomUUID());
        ClienteId idCliente = new ClienteId(UUID.randomUUID());

        DireccionEnvio direccion = new DireccionEnvio(
                "Juan Perez",
                "Calle Falsa 123",
                "Ciudad de Mexico",
                "CDMX",
                "12345",
                "Mexico",
                "5512345678",
                "Casa blanca");

        ProductoId prodId = new ProductoId(UUID.randomUUID());
        Money precio = new Money(java.math.BigDecimal.valueOf(500), "MXN");

        ItemOrden item1 = new ItemOrden(
                new ItemOrdenId(UUID.randomUUID()),
                prodId,
                "Producto 1",
                "ABC-111",
                2,
                precio);

        List<ItemOrden> listaItems = new ArrayList<>();
        listaItems.add(item1);

        Money descuento = new Money(java.math.BigDecimal.valueOf(100), "MXN");

        Orden orden = Orden.crear(
                idOrden,
                "ORD-001",
                idCliente,
                listaItems,
                direccion,
                descuento);

        assertNotNull(orden);
        assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
        assertEquals(0, orden.getSubtotal().getCantidad().compareTo(java.math.BigDecimal.valueOf(1000)));
        assertEquals(0, orden.getTotal().getCantidad().compareTo(java.math.BigDecimal.valueOf(900)));
    }

    @Test
    @DisplayName("Debe lanzar excepción si se crea una orden sin items")
    void testErrorSinItems() {
        OrdenId id = new OrdenId(UUID.randomUUID());
        ClienteId cliente = new ClienteId(UUID.randomUUID());

        DireccionEnvio dir = new DireccionEnvio(
                "A", "C", "C", "E",
                "12345", "Mexico",
                "5512345678", "");

        Money desc = new Money(java.math.BigDecimal.ZERO, "MXN");
        List<ItemOrden> listaVacia = new ArrayList<>();

        assertThrows(IllegalArgumentException.class,
                () -> Orden.crear(id, "ORD-ERR", cliente, listaVacia, dir, desc));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el descuento es mayor al subtotal")
    void testErrorTotalNegativo() {
        ItemOrden item = new ItemOrden(
                new ItemOrdenId(UUID.randomUUID()),
                new ProductoId(UUID.randomUUID()),
                "P",
                "ABC-111",
                1,
                new Money(java.math.BigDecimal.valueOf(100), "MXN"));

        List<ItemOrden> items = new ArrayList<>();
        items.add(item);

        Money descuentoGrande = new Money(java.math.BigDecimal.valueOf(200), "MXN");

        assertThrows(IllegalArgumentException.class,
                () -> Orden.crear(
                        new OrdenId(UUID.randomUUID()),
                        "O",
                        new ClienteId(UUID.randomUUID()),
                        items,
                        new DireccionEnvio("A", "C", "C", "E",
                                "12345", "Mexico", "5512345678", ""),
                        descuentoGrande));
    }

    @Test
    @DisplayName("Debe completar correctamente el ciclo de vida de la orden")
    void testCicloCompleto() {
        ItemOrden item = new ItemOrden(
                new ItemOrdenId(UUID.randomUUID()),
                new ProductoId(UUID.randomUUID()),
                "P",
                "ABC-111",
                1,
                new Money(java.math.BigDecimal.valueOf(1000), "MXN"));

        List<ItemOrden> items = new ArrayList<>();
        items.add(item);

        Orden orden = Orden.crear(
                new OrdenId(UUID.randomUUID()),
                "O-1",
                new ClienteId(UUID.randomUUID()),
                items,
                new DireccionEnvio("A", "C", "C", "E",
                        "12345", "Mexico", "5512345678", ""),
                new Money(java.math.BigDecimal.ZERO, "MXN"));

        orden.confirmar("juan");
        assertEquals(EstadoOrden.CONFIRMADA, orden.getEstado());

        orden.procesarPago("TARJETA", "1234", "juan");
        assertEquals(EstadoOrden.PAGO_PROCESADO, orden.getEstado());

        orden.marcarEnProceso("almacen");
        assertEquals(EstadoOrden.EN_PREPARACION, orden.getEstado());

        InfoEnvio envio = new InfoEnvio("DHL", "MX-1234567890", LocalDateTime.now());
        orden.marcarEnviada(envio, "logistica");
        assertEquals(EstadoOrden.ENVIADA, orden.getEstado());

        orden.marcarEntregada("repartidor");
        assertEquals(EstadoOrden.ENTREGADA, orden.getEstado());
    }

    @Test
    @DisplayName("No debe permitir confirmar una orden ya confirmada")
    void testNoConfirmarSiNoEsPendiente() {
        ItemOrden item = new ItemOrden(
                new ItemOrdenId(UUID.randomUUID()),
                new ProductoId(UUID.randomUUID()),
                "P",
                "ABC-111",
                1,
                new Money(java.math.BigDecimal.valueOf(100), "MXN"));

        List<ItemOrden> items = new ArrayList<>();
        items.add(item);

        Orden orden = Orden.crear(
                new OrdenId(UUID.randomUUID()),
                "O",
                new ClienteId(UUID.randomUUID()),
                items,
                new DireccionEnvio("A", "C", "C", "E",
                        "12345", "Mexico", "5512345678", ""),
                new Money(java.math.BigDecimal.ZERO, "MXN"));

        orden.confirmar("yo");

        assertThrows(IllegalStateException.class,
                () -> orden.confirmar("yo"));
    }

    @Test
    @DisplayName("Debe cancelar correctamente una orden")
    void testCancelar() {
        ItemOrden item = new ItemOrden(
                new ItemOrdenId(UUID.randomUUID()),
                new ProductoId(UUID.randomUUID()),
                "P",
                "ABC-111",
                1,
                new Money(java.math.BigDecimal.valueOf(100), "MXN"));

        List<ItemOrden> items = new ArrayList<>();
        items.add(item);

        Orden orden = Orden.crear(
                new OrdenId(UUID.randomUUID()),
                "O",
                new ClienteId(UUID.randomUUID()),
                items,
                new DireccionEnvio("A", "C", "C", "E",
                        "12345", "Mexico", "5512345678", ""),
                new Money(java.math.BigDecimal.ZERO, "MXN"));

        orden.cancelar("El cliente se arrepintio", "admin");

        assertEquals(EstadoOrden.CANCELADA, orden.getEstado());
    }
}