package com.example.demo.integration;

import com.example.demo.catalogo.domain.Categoria;
import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.catalogo.domain.Producto;
import com.example.demo.catalogo.repository.CategoriaJpaRepository;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.dto.ApiError;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.domain.Money;
import com.example.demo.ventas.domain.Carrito;
import com.example.demo.ventas.domain.CarritoId;
import com.example.demo.ventas.dto.AgregarProductoRequest;
import com.example.demo.ventas.repository.CarritoRepository;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarritoControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/carritos";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoJpaRepository productoRepository;

    @Autowired
    private CategoriaJpaRepository categoriaRepository;

    private UUID productoId;
    private UUID categoriaId;

    private String baseUrl() {
        return "http://localhost:" + port + BASE_URL;
    }

    @BeforeEach
    void setUp() {
        carritoRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Crear categoría
        Categoria categoria = new Categoria(CategoriaId.generar(), "Electrónica", "Productos electrónicos", null);
        categoria = categoriaRepository.save(categoria);
        categoriaId = categoria.getId().id();

        // Crear producto con SKU
        Producto producto = Producto.crear(
                "Laptop Gamer",
                "Laptop de alto rendimiento",
                Money.pesos(25000),
                10,
                "ABC-123",
                new CategoriaId(categoriaId)
        );
        producto = productoRepository.save(producto);
        productoId = producto.getId().getValue();
    }

    @AfterEach
    void cleanUp() {
        carritoRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/carritos - Debe crear carrito con clienteId válido")
    void crearCarrito_ClienteIdValido_Retorna201() {
        UUID clienteId = UUID.randomUUID();
        ResponseEntity<Carrito> response = restTemplate.postForEntity(
                baseUrl() + "?clienteId=" + clienteId, null, Carrito.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        UUID carritoId = response.getBody().getId().getValor();

        // Verificar que se haya guardado correctamente obteniéndolo por ID
        ResponseEntity<Carrito> getResponse = restTemplate.getForEntity(
                baseUrl() + "/" + carritoId, Carrito.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(clienteId, getResponse.getBody().getClienteId().getValue());
    }

    @Test
    @DisplayName("GET /api/v1/carritos/{id} - Debe retornar carrito existente")
    void obtenerCarrito_Existente_Retorna200() {
        UUID clienteId = UUID.randomUUID();
        ResponseEntity<Carrito> createResponse = restTemplate.postForEntity(
                baseUrl() + "?clienteId=" + clienteId, null, Carrito.class);
        UUID carritoId = createResponse.getBody().getId().getValor();

        ResponseEntity<Carrito> response = restTemplate.getForEntity(
                baseUrl() + "/" + carritoId, Carrito.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(carritoId, response.getBody().getId().getValor());
    }

    @Test
    @DisplayName("GET /api/v1/carritos/{id} - Debe retornar 404 si carrito no existe")
    void obtenerCarrito_NoExistente_Retorna404() {
        UUID idInexistente = UUID.randomUUID();
        ResponseEntity<ApiError> response = restTemplate.getForEntity(
                baseUrl() + "/" + idInexistente, ApiError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }

    @Test
    @DisplayName("POST /api/v1/carritos/{id}/productos - Debe agregar producto al carrito")
    void agregarProducto_DatosValidos_Retorna200() {
        UUID clienteId = UUID.randomUUID();
        ResponseEntity<Carrito> createResponse = restTemplate.postForEntity(
                baseUrl() + "?clienteId=" + clienteId, null, Carrito.class);
        UUID carritoId = createResponse.getBody().getId().getValor();

        AgregarProductoRequest request = new AgregarProductoRequest();
        request.setProductoId(productoId);
        request.setCantidad(2);

        HttpEntity<AgregarProductoRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<Carrito> response = restTemplate.exchange(
                baseUrl() + "/" + carritoId + "/productos",
                HttpMethod.POST,
                httpEntity,
                Carrito.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getItems().size());
        assertEquals(productoId, response.getBody().getItems().get(0).getProductoId().getValue());
        assertEquals(2, response.getBody().getItems().get(0).getCantidad());
    }

    @Test
    @DisplayName("POST /api/v1/carritos/{id}/productos - Debe retornar 404 si producto no existe")
    void agregarProducto_ProductoNoExiste_Retorna404() {
        UUID clienteId = UUID.randomUUID();
        ResponseEntity<Carrito> createResponse = restTemplate.postForEntity(
                baseUrl() + "?clienteId=" + clienteId, null, Carrito.class);
        UUID carritoId = createResponse.getBody().getId().getValor();

        AgregarProductoRequest request = new AgregarProductoRequest();
        request.setProductoId(UUID.randomUUID());
        request.setCantidad(1);

        HttpEntity<AgregarProductoRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<ApiError> response = restTemplate.exchange(
                baseUrl() + "/" + carritoId + "/productos",
                HttpMethod.POST,
                httpEntity,
                ApiError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("PATCH /api/v1/carritos/{id}/productos/{productoId} - Debe modificar cantidad")
    void modificarCantidad_DatosValidos_Retorna200() {
        UUID clienteId = UUID.randomUUID();
        ResponseEntity<Carrito> createResponse = restTemplate.postForEntity(
                baseUrl() + "?clienteId=" + clienteId, null, Carrito.class);
        UUID carritoId = createResponse.getBody().getId().getValor();

        AgregarProductoRequest addRequest = new AgregarProductoRequest();
        addRequest.setProductoId(productoId);
        addRequest.setCantidad(2);
        restTemplate.postForEntity(baseUrl() + "/" + carritoId + "/productos", addRequest, Carrito.class);

        int nuevaCantidad = 5;
        RestTemplate patchRestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<Carrito> response = patchRestTemplate.exchange(
                baseUrl() + "/" + carritoId + "/productos/" + productoId + "?nuevaCantidad=" + nuevaCantidad,
                HttpMethod.PATCH,
                null,
                Carrito.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(nuevaCantidad, response.getBody().getItems().get(0).getCantidad());
    }

    @Test
    @DisplayName("DELETE /api/v1/carritos/{id}/productos/{productoId} - Debe eliminar producto")
    void eliminarProducto_ProductoExistente_Retorna200() {
        UUID clienteId = UUID.randomUUID();
        ResponseEntity<Carrito> createResponse = restTemplate.postForEntity(
                baseUrl() + "?clienteId=" + clienteId, null, Carrito.class);
        UUID carritoId = createResponse.getBody().getId().getValor();

        AgregarProductoRequest addRequest = new AgregarProductoRequest();
        addRequest.setProductoId(productoId);
        addRequest.setCantidad(2);
        restTemplate.postForEntity(baseUrl() + "/" + carritoId + "/productos", addRequest, Carrito.class);

        ResponseEntity<Carrito> response = restTemplate.exchange(
                baseUrl() + "/" + carritoId + "/productos/" + productoId,
                HttpMethod.DELETE,
                null,
                Carrito.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getItems().isEmpty());
    }

    @Test
    @DisplayName("DELETE /api/v1/carritos/{id}/vaciar - Debe vaciar el carrito")
    void vaciarCarrito_ConItems_Retorna200() {
        UUID clienteId = UUID.randomUUID();
        ResponseEntity<Carrito> createResponse = restTemplate.postForEntity(
                baseUrl() + "?clienteId=" + clienteId, null, Carrito.class);
        UUID carritoId = createResponse.getBody().getId().getValor();

        AgregarProductoRequest addRequest = new AgregarProductoRequest();
        addRequest.setProductoId(productoId);
        addRequest.setCantidad(2);
        restTemplate.postForEntity(baseUrl() + "/" + carritoId + "/productos", addRequest, Carrito.class);

        ResponseEntity<Carrito> response = restTemplate.exchange(
                baseUrl() + "/" + carritoId + "/vaciar",
                HttpMethod.DELETE,
                null,
                Carrito.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getItems().isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/carritos/{id}/checkout/iniciar - Debe iniciar checkout")
    void iniciarCheckout_CarritoConItems_Retorna200() {
        UUID clienteId = UUID.randomUUID();
        ResponseEntity<Carrito> createResponse = restTemplate.postForEntity(
                baseUrl() + "?clienteId=" + clienteId, null, Carrito.class);
        UUID carritoId = createResponse.getBody().getId().getValor();

        AgregarProductoRequest addRequest = new AgregarProductoRequest();
        addRequest.setProductoId(productoId);
        addRequest.setCantidad(2);
        restTemplate.postForEntity(baseUrl() + "/" + carritoId + "/productos", addRequest, Carrito.class);

        ResponseEntity<Carrito> response = restTemplate.postForEntity(
                baseUrl() + "/" + carritoId + "/checkout/iniciar", null, Carrito.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("EN_CHECKOUT", response.getBody().getEstado().name());
    }
}