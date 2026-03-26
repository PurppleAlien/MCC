package com.example.demo.api;

import com.example.demo.catalogo.api.CatalogoApi;
import com.example.demo.catalogo.api.ProductoResumen;
import com.example.demo.catalogo.domain.Categoria;
import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.catalogo.domain.Producto;
import com.example.demo.catalogo.repository.CategoriaJpaRepository;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.shared.exception.StockInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CatalogoApiIntegrationTest {

    @Autowired
    private CatalogoApi catalogoApi;

    @Autowired
    private ProductoJpaRepository productoRepository;

    @Autowired
    private CategoriaJpaRepository categoriaRepository;

    private ProductoId productoId;
    private CategoriaId categoriaId;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();

        Categoria categoria = new Categoria(CategoriaId.generar(), "Electrónica", "Productos electrónicos", null);
        categoria = categoriaRepository.save(categoria);
        categoriaId = categoria.getId();

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
    }

    @Test
    void obtenerProductoExistente() {
        ProductoResumen resumen = catalogoApi.obtenerProducto(productoId);
        assertNotNull(resumen);
        assertEquals(productoId, resumen.id());
        assertEquals("Laptop Gamer", resumen.nombre());
        assertEquals(10, resumen.stock());
        assertEquals("ABC-123", resumen.sku());
        assertEquals(Money.pesos(25000), resumen.precio());
    }

    @Test
    void obtenerProductoNoExistenteLanzaExcepcion() {
        ProductoId idInexistente = ProductoId.generar();
        assertThrows(RecursoNoEncontradoException.class,
                () -> catalogoApi.obtenerProducto(idInexistente));
    }

    @Test
    void validarStockSuficiente() {
        assertDoesNotThrow(() -> catalogoApi.validarStock(productoId, 5));
    }

    @Test
    void validarStockInsuficienteLanzaExcepcion() {
        assertThrows(StockInsuficienteException.class,
                () -> catalogoApi.validarStock(productoId, 20));
    }
}