package com.example.demo.integration;

import com.example.demo.catalogo.domain.Categoria;
import com.example.demo.catalogo.domain.CategoriaId;
import com.example.demo.catalogo.dto.CategoriaRequest;
import com.example.demo.catalogo.dto.CategoriaResponse;
import com.example.demo.catalogo.repository.CategoriaJpaRepository;
import com.example.demo.dto.ApiError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoriaControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/categorias";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CategoriaJpaRepository categoriaRepository;

    @AfterEach
    void cleanUp() {
        categoriaRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/categorias - Debe crear categoría con datos válidos")
    void crearCategoria_DatosValidos_Retorna201() {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("Electrónica");
        request.setDescripcion("Productos electrónicos");

        HttpEntity<CategoriaRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<CategoriaResponse> response = restTemplate.exchange(
                BASE_URL, HttpMethod.POST, httpEntity, CategoriaResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Electrónica", response.getBody().getNombre());
    }

    @Test
    @DisplayName("POST /api/v1/categorias - Debe retornar 400 con nombre vacío")
    void crearCategoria_NombreVacio_Retorna400() {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("");
        request.setDescripcion("Descripción");

        HttpEntity<CategoriaRequest> httpEntity = new HttpEntity<>(request);
        ResponseEntity<ApiError> response = restTemplate.exchange(
                BASE_URL, HttpMethod.POST, httpEntity, ApiError.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }
}