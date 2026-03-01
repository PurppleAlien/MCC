package com.example.demo.api;

import com.example.demo.catalogo.api.CatalogoApi;
import com.example.demo.catalogo.domain.Categoria;
import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.catalogo.domain.Producto;
import com.example.demo.catalogo.repository.CategoriaJpaRepository;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.ventas.api.CarritoResumen;
import com.example.demo.ventas.api.VentasApi;
import com.example.demo.ventas.domain.Carrito;
import com.example.demo.ventas.domain.CarritoId;
import com.example.demo.ventas.domain.ProductoRef;
import com.example.demo.ventas.repository.CarritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VentasApiIntegrationTest {

    @Autowired
    private VentasApi ventasApi;

    @Autowired
    private CatalogoApi catalogoApi;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoJpaRepository productoRepository;

    @Autowired
    private CategoriaJpaRepository categoriaRepository;

    private UUID carritoId;
    private ProductoId productoId;
    private ClienteId clienteId;

    @BeforeEach
    void setUp() {
        carritoRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();

        Categoria categoria = new Categoria(CategoriaId.generar(), "Electrónica", "Productos electrónicos", null);
        categoria = categoriaRepository.save(categoria);
        CategoriaId categoriaId = categoria.getId();

        Producto producto = Producto.crear(
                "Laptop Gamer",
                "Laptop de alto rendimiento",
                Money.pesos(25000),
                10,
                "ABC-123",
                categoriaId
        );
        producto = productoRepository.save(producto);
        productoId = producto.getId();

        clienteId = new ClienteId(UUID.randomUUID());
        Carrito carrito = new Carrito(CarritoId.generar(), clienteId);
        carrito = carritoRepository.save(carrito);
        carritoId = carrito.getId().getValor();

        // Agregar producto al carrito usando la API de catálogo
        var productoResumen = catalogoApi.obtenerProducto(productoId);
        ProductoRef ref = new ProductoRef(productoId, productoResumen.precio());
        carrito.agregarProducto(ref, 2);
        carritoRepository.save(carrito);
    }

    @Test
    void obtenerCarritoExistente() {
        CarritoResumen resumen = ventasApi.obtenerCarrito(carritoId);
        assertNotNull(resumen);
        assertEquals(carritoId, resumen.carritoId());
        assertEquals(clienteId, resumen.clienteId());
        assertEquals("ACTIVO", resumen.estado());
        assertEquals(1, resumen.items().size());
        var item = resumen.items().get(0);
        assertEquals(productoId, item.productoId());
        assertEquals(2, item.cantidad());
        assertEquals(Money.pesos(25000), item.precioUnitario());
    }

    @Test
    void obtenerCarritoNoExistenteLanzaExcepcion() {
        UUID idInexistente = UUID.randomUUID();
        assertThrows(RecursoNoEncontradoException.class,
                () -> ventasApi.obtenerCarrito(idInexistente));
    }

    @Test
    void completarCheckoutCambiaEstado() {
        // Iniciar checkout manualmente (es interno, no expuesto por la API)
        Carrito carrito = carritoRepository.findById(new CarritoId(carritoId)).orElseThrow();
        carrito.iniciarCheckout();
        carritoRepository.save(carrito);

        ventasApi.completarCheckout(carritoId);

        CarritoResumen resumen = ventasApi.obtenerCarrito(carritoId);
        assertEquals("COMPLETADO", resumen.estado());
    }

    @Test
    void completarCheckoutCarritoNoEnCheckoutLanzaExcepcion() {
        assertThrows(IllegalStateException.class,
                () -> ventasApi.completarCheckout(carritoId));
    }
}