package com.example.demo.ventas.service;

import com.example.demo.catalogo.api.CatalogoApi;
import com.example.demo.catalogo.api.ProductoResumen;
import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.shared.exception.StockInsuficienteException;
import com.example.demo.ventas.domain.*;
import com.example.demo.ventas.repository.CarritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private CatalogoApi catalogoApi;

    @InjectMocks
    private CarritoService carritoService;

    private CarritoId carritoId;
    private ProductoId productoId;
    private ClienteId clienteId;
    private ProductoResumen productoResumen;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        carritoId = CarritoId.generar();
        productoId = ProductoId.generar();
        clienteId = new ClienteId(UUID.randomUUID());
        // Creamos un ProductoResumen con los mismos datos que tendría un producto real
        productoResumen = new ProductoResumen(
                productoId,
                "Producto Test",
                Money.pesos(100),
                10,
                "ABC-123"
        );
        carrito = new Carrito(carritoId, clienteId);
    }

    @Test
    @DisplayName("obtenerOCrear debe retornar carrito existente")
    void obtenerOCrear_cuandoExiste_retornaCarrito() {
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));

        Carrito resultado = carritoService.obtenerOCrear(carritoId, clienteId);

        assertNotNull(resultado);
        assertEquals(carritoId, resultado.getId());
        assertEquals(clienteId, resultado.getClienteId());
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    @DisplayName("obtenerOCrear debe crear y guardar carrito nuevo si no existe")
    void obtenerOCrear_cuandoNoExiste_creaYGuarda() {
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.obtenerOCrear(carritoId, clienteId);

        assertNotNull(resultado);
        assertEquals(carritoId, resultado.getId());
        assertEquals(clienteId, resultado.getClienteId());
        assertEquals(EstadoCarrito.ACTIVO, resultado.getEstado());
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    @DisplayName("crear debe guardar un nuevo carrito")
    void crear_correcto() {
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.crear(clienteId);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(clienteId, resultado.getClienteId());
        assertEquals(EstadoCarrito.ACTIVO, resultado.getEstado());
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    @DisplayName("agregarProducto debe agregar producto al carrito correctamente")
    void agregarProducto_correcto() {
        int cantidad = 2;

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(catalogoApi.obtenerProducto(productoId)).thenReturn(productoResumen);
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.agregarProducto(carritoId, productoId, cantidad);

        assertNotNull(resultado);
        assertEquals(1, resultado.getItems().size());
        ItemCarrito item = resultado.getItems().get(0);
        assertEquals(productoId, item.getProductoId());
        assertEquals(cantidad, item.getCantidad());
        assertEquals(productoResumen.precio(), item.getPrecioUnitario());
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("agregarProducto debe lanzar excepción si producto no existe")
    void agregarProducto_productoNoExistente_lanzaExcepcion() {
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(catalogoApi.obtenerProducto(productoId))
                .thenThrow(new RecursoNoEncontradoException("Producto", productoId.getValue()));

        assertThrows(RecursoNoEncontradoException.class,
                () -> carritoService.agregarProducto(carritoId, productoId, 1));

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("agregarProducto debe lanzar excepción si stock insuficiente")
    void agregarProducto_stockInsuficiente_lanzaExcepcion() {
        int cantidad = 20; // mayor al stock (10)
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(catalogoApi.obtenerProducto(productoId)).thenReturn(productoResumen); // stock 10

        assertThrows(StockInsuficienteException.class,
                () -> carritoService.agregarProducto(carritoId, productoId, cantidad));

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("modificarCantidad debe actualizar cantidad del item")
    void modificarCantidad_correcto() {
        // Primero agregamos un producto manualmente al carrito
        ProductoRef ref = new ProductoRef(productoId, productoResumen.precio());
        carrito.agregarProducto(ref, 2);

        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int nuevaCantidad = 5;
        Carrito resultado = carritoService.modificarCantidad(carritoId, productoId, nuevaCantidad);

        assertEquals(1, resultado.getItems().size());
        assertEquals(nuevaCantidad, resultado.getItems().get(0).getCantidad());
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("modificarCantidad lanza excepción si carrito no existe")
    void modificarCantidad_carritoNoExiste_lanzaExcepcion() {
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> carritoService.modificarCantidad(carritoId, productoId, 3));

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("eliminarProducto debe remover item del carrito")
    void eliminarProducto_correcto() {
        ProductoRef ref = new ProductoRef(productoId, productoResumen.precio());
        carrito.agregarProducto(ref, 2);
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.eliminarProducto(carritoId, productoId);

        assertTrue(resultado.getItems().isEmpty());
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("vaciar debe remover todos los items del carrito")
    void vaciar_correcto() {
        ProductoRef ref1 = new ProductoRef(productoId, productoResumen.precio());
        ProductoRef ref2 = new ProductoRef(ProductoId.generar(), Money.pesos(50));
        carrito.agregarProducto(ref1, 2);
        carrito.agregarProducto(ref2, 1);
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.vaciar(carritoId);

        assertTrue(resultado.getItems().isEmpty());
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("iniciarCheckout cambia estado a EN_CHECKOUT")
    void iniciarCheckout_correcto() {
        ProductoRef ref = new ProductoRef(productoId, productoResumen.precio());
        carrito.agregarProducto(ref, 1);
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.iniciarCheckout(carritoId);

        assertEquals(EstadoCarrito.EN_CHECKOUT, resultado.getEstado());
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("completarCheckout cambia estado a COMPLETADO")
    void completarCheckout_correcto() {
        ProductoRef ref = new ProductoRef(productoId, productoResumen.precio());
        carrito.agregarProducto(ref, 1);
        carrito.iniciarCheckout(); // necesario para poder completar
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.completarCheckout(carritoId);

        assertEquals(EstadoCarrito.COMPLETADO, resultado.getEstado());
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("abandonar cambia estado a ABANDONADO")
    void abandonar_correcto() {
        ProductoRef ref = new ProductoRef(productoId, productoResumen.precio());
        carrito.agregarProducto(ref, 1);
        carrito.iniciarCheckout();
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.abandonar(carritoId);

        assertEquals(EstadoCarrito.ABANDONADO, resultado.getEstado());
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("obtenerCarrito retorna carrito si existe")
    void obtenerCarrito_cuandoExiste_retornaCarrito() {
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.of(carrito));

        Carrito resultado = carritoService.obtenerCarrito(carritoId);

        assertNotNull(resultado);
        assertEquals(carritoId, resultado.getId());
    }

    @Test
    @DisplayName("obtenerCarrito lanza excepción si carrito no existe")
    void obtenerCarrito_cuandoNoExiste_lanzaExcepcion() {
        when(carritoRepository.findById(carritoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> carritoService.obtenerCarrito(carritoId));
    }
}