package com.example.demo.catalogo.api;

import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.shared.exception.StockInsuficienteException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogoApiHttpClient implements CatalogoApi {

    private final RestTemplate restTemplate;
    private final String catalogoBaseUrl;

    public CatalogoApiHttpClient(RestTemplate restTemplate,
                                 @Value("${catalogo.service.url}") String catalogoBaseUrl) {
        this.restTemplate = restTemplate;
        this.catalogoBaseUrl = catalogoBaseUrl;
    }

    @Override
    public ProductoResumen obtenerProducto(ProductoId id) throws RecursoNoEncontradoException {
        String url = catalogoBaseUrl + "/api/v1/productos/" + id.getValue();
        try {
            return restTemplate.getForObject(url, ProductoResumen.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RecursoNoEncontradoException("Producto", id.getValue());
            }
            throw e;
        }
    }

    @Override
    public void validarStock(ProductoId id, int cantidad) throws StockInsuficienteException, RecursoNoEncontradoException {
        ProductoResumen producto = obtenerProducto(id);
        if (producto.stock() < cantidad) {
            throw new StockInsuficienteException(producto.nombre(), cantidad, producto.stock());
        }
    }
}