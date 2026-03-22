package com.example.demo.integration;

import com.example.demo.catalogo.domain.Categoria;
import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.catalogo.dto.ProductoRequest;
import com.example.demo.catalogo.dto.ProductoResponse;
import com.example.demo.catalogo.repository.CategoriaJpaRepository;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.dto.ApiError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductoControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/productos";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductoJpaRepository productoRepository;

    @Autowired
    private CategoriaJpaRepository categoriaRepository;

    private UUID categoriaId;

    private String baseUrl() {
        return "http://localhost:" + port + BASE_URL;
    }

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();

        Categoria categoria = new Categoria(CategoriaId.generar(), "Electrónica", "Productos electrónicos", null);
        categoria = categoriaRepository.save(categoria);
        categoriaId = categoria.getId().id();
    }

    @AfterEach
    void cleanUp() {
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/productos - Debe crear producto con datos válidos")
    void crearProducto_DatosValidos_Retorna201() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Laptop Gamer");
        request.setDescripcion("Laptop de alto rendimiento");
        request.setPrecio(BigDecimal.valueOf(25000));
        request.setMoneda("MXN");
        request.setStock(10);
        request.setCategoriaId(categoriaId);
        request.setSku("ABC-123");

        HttpEntity<ProductoRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<ProductoResponse> response = restTemplate.exchange(
                baseUrl(), HttpMethod.POST, httpEntity, ProductoResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Laptop Gamer", response.getBody().getNombre());
        assertEquals(BigDecimal.valueOf(25000), response.getBody().getPrecio().getCantidad());
        assertEquals(10, response.getBody().getStock());
        assertEquals("ABC-123", response.getBody().getSku());
    }

    @Test
    @DisplayName("POST /api/v1/productos - Debe retornar 400 con datos inválidos")
    void crearProducto_DatosInvalidos_Retorna400() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre(""); // nombre vacío
        request.setPrecio(BigDecimal.valueOf(-100)); // precio negativo
        // No se envía categoría ni otros campos obligatorios

        HttpEntity<ProductoRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<ApiError> response = restTemplate.exchange(
                baseUrl(), HttpMethod.POST, httpEntity, ApiError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Validation Error", response.getBody().getError());
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} - Debe retornar 404 si producto no existe")
    void obtenerProducto_NoExiste_Retorna404() {
        UUID idInexistente = UUID.randomUUID();
        ResponseEntity<ApiError> response = restTemplate.getForEntity(
                baseUrl() + "/" + idInexistente, ApiError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }
}