package com.uamishop.ventas.clients;

import com.uamishop.ventas.shared.domain.Money;
import com.uamishop.ventas.shared.domain.ProductoId;
import com.uamishop.ventas.shared.exception.RecursoNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogoApiClient {

    private final RestTemplate restTemplate;
    private final String catalogoUrl;

    public CatalogoApiClient(RestTemplate restTemplate,
                             @Value("${catalogo.api.url}") String catalogoUrl) {
        this.restTemplate = restTemplate;
        this.catalogoUrl = catalogoUrl;
    }

    public ProductoInfo obtenerProducto(ProductoId productoId) {
        String url = catalogoUrl + "/api/v1/productos/" + productoId.getValue();
        try {
            ProductoInfo info = restTemplate.getForObject(url, ProductoInfo.class);
            if (info == null) {
                throw new RecursoNoEncontradoException("Producto", productoId.getValue());
            }
            return info;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RecursoNoEncontradoException("Producto", productoId.getValue());
            }
            throw e;
        }
    }

    public record ProductoInfo(
        String id,
        String nombre,
        String sku,
        Money precio,
        Integer stock
    ) {}
}
