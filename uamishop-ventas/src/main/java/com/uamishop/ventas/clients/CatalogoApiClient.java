package com.uamishop.ventas.clients;

import com.uamishop.ventas.shared.domain.ProductoId;
import com.uamishop.ventas.shared.domain.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
        return restTemplate.getForObject(url, ProductoInfo.class);
    }

    public record ProductoInfo(
        String id,
        String nombre,
        String sku,
        Money precio,
        Integer stock
    ) {}
}
