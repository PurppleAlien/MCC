package com.example.demo.integration;

import com.example.demo.catalogo.domain.Categoria;
import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.catalogo.domain.Producto;
import com.example.demo.shared.domain.ProductoId; // <-- IMPORTACIÓN AGREGADA
import com.example.demo.catalogo.repository.CategoriaJpaRepository;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.dto.ApiError;
import com.example.demo.ordenes.domain.*;
import com.example.demo.ordenes.dto.ItemOrdenRequest;
import com.example.demo.ordenes.dto.OrdenRequest;
import com.example.demo.ordenes.dto.OrdenResponse;
import com.example.demo.ordenes.repository.OrdenJpaRepository;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrdenControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/ordenes";
    private static final String CARRO_URL = "/api/v1/carritos";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrdenJpaRepository ordenRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoJpaRepository productoRepository;

    @Autowired
    private CategoriaJpaRepository categoriaRepository;

    private UUID productoId;
    private UUID categoriaId;
    private UUID carritoId;

    private String baseUrl() {
        return "http://localhost:" + port + BASE_URL;
    }

    private String carroUrl() {
        return "http://localhost:" + port + CARRO_URL;
    }

    @BeforeEach
    void setUp() {
        ordenRepository.deleteAll();
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

        // Crear carrito para pruebas
        UUID clienteId = UUID.randomUUID();
        Carrito carrito = new Carrito(CarritoId.generar(), new ClienteId(clienteId));
        carrito = carritoRepository.save(carrito);
        carritoId = carrito.getId().getValor();
    }

    @AfterEach
    void cleanUp() {
        ordenRepository.deleteAll();
        carritoRepository.deleteAll();
        productoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/ordenes - Debe crear orden con datos válidos")
    void crearOrden_DatosValidos_Retorna201() {
        ItemOrdenRequest itemRequest = new ItemOrdenRequest();
        itemRequest.setProductoId(new ProductoId(productoId));
        itemRequest.setNombreProducto("Laptop Gamer");
        itemRequest.setSku("ABC-123");
        itemRequest.setCantidad(2);
        itemRequest.setPrecioUnitario(Money.pesos(25000));

        OrdenRequest request = new OrdenRequest();
        request.setNumeroOrden("ORD-001");
        request.setClienteId(new ClienteId(UUID.randomUUID()));
        request.setItems(List.of(itemRequest));
        request.setDireccionEnvio(new DireccionEnvio(
                "Juan Pérez",
                "Calle Falsa 123",
                "Ciudad de México",
                "CDMX",
                "12345",
                "México",
                "5512345678",
                "Casa blanca"
        ));
        request.setDescuento(Money.pesos(0));

        HttpEntity<OrdenRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<OrdenResponse> response = restTemplate.exchange(
                baseUrl(), HttpMethod.POST, httpEntity, OrdenResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("ORD-001", response.getBody().getNumeroOrden());
        assertEquals(1, response.getBody().getItems().size());
    }

    @Test
    @DisplayName("POST /api/v1/ordenes/desde-carrito/{carritoId} - Debe crear orden desde carrito")
    void crearOrdenDesdeCarrito_CarritoValido_Retorna201() {
        // Agregar producto al carrito
        AgregarProductoRequest addRequest = new AgregarProductoRequest();
        addRequest.setProductoId(productoId);
        addRequest.setCantidad(2);
        restTemplate.postForEntity(carroUrl() + "/" + carritoId + "/productos", addRequest, Carrito.class);

        // Iniciar checkout en el carrito (necesario para poder completarlo después)
        restTemplate.postForEntity(carroUrl() + "/" + carritoId + "/checkout/iniciar", null, Carrito.class);

        DireccionEnvio direccion = new DireccionEnvio(
                "Juan Pérez",
                "Calle Falsa 123",
                "Ciudad de México",
                "CDMX",
                "12345",
                "México",
                "5512345678",
                "Casa blanca"
        );

        HttpEntity<DireccionEnvio> httpEntity = new HttpEntity<>(direccion);
        ResponseEntity<OrdenResponse> response = restTemplate.exchange(
                baseUrl() + "/desde-carrito/" + carritoId,
                HttpMethod.POST,
                httpEntity,
                OrdenResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getItems().size());
    }

    @Test
    @DisplayName("GET /api/v1/ordenes/{id} - Debe retornar orden existente")
    void obtenerOrden_Existente_Retorna200() {
        OrdenRequest request = crearOrdenRequestValida();
        ResponseEntity<OrdenResponse> createResponse = restTemplate.postForEntity(
                baseUrl(), request, OrdenResponse.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        UUID ordenId = createResponse.getBody().getId().getValue();

        ResponseEntity<OrdenResponse> response = restTemplate.getForEntity(
                baseUrl() + "/" + ordenId, OrdenResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ordenId, response.getBody().getId().getValue());
    }

    @Test
    @DisplayName("GET /api/v1/ordenes/{id} - Debe retornar 404 si orden no existe")
    void obtenerOrden_NoExistente_Retorna404() {
        UUID idInexistente = UUID.randomUUID();
        ResponseEntity<ApiError> response = restTemplate.getForEntity(
                baseUrl() + "/" + idInexistente, ApiError.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }

    @Test
    @DisplayName("PATCH /api/v1/ordenes/{id}/confirmar - Debe confirmar orden")
    void confirmarOrden_OrdenPendiente_Retorna200() {
        OrdenRequest request = crearOrdenRequestValida();
        ResponseEntity<OrdenResponse> createResponse = restTemplate.postForEntity(
                baseUrl(), request, OrdenResponse.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        UUID ordenId = createResponse.getBody().getId().getValue();

        RestTemplate patchRestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<OrdenResponse> response = patchRestTemplate.exchange(
                baseUrl() + "/" + ordenId + "/confirmar?usuario=juan",
                HttpMethod.PATCH,
                null,
                OrdenResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CONFIRMADA", response.getBody().getEstado().name());
    }

    private OrdenRequest crearOrdenRequestValida() {
        ItemOrdenRequest itemRequest = new ItemOrdenRequest();
        itemRequest.setProductoId(new ProductoId(productoId));
        itemRequest.setNombreProducto("Laptop Gamer");
        itemRequest.setSku("ABC-123");
        itemRequest.setCantidad(2);
        itemRequest.setPrecioUnitario(Money.pesos(25000));

        OrdenRequest request = new OrdenRequest();
        request.setNumeroOrden("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        request.setClienteId(new ClienteId(UUID.randomUUID()));
        request.setItems(List.of(itemRequest));
        request.setDireccionEnvio(new DireccionEnvio(
                "Juan Pérez",
                "Calle Falsa 123",
                "Ciudad de México",
                "CDMX",
                "12345",
                "México",
                "5512345678",
                "Casa blanca"
        ));
        request.setDescuento(Money.pesos(0));
        return request;
    }
}