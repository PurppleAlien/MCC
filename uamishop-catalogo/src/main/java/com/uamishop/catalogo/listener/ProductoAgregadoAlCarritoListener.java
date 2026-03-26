package com.uamishop.catalogo.listener;

import com.uamishop.catalogo.service.ProductoEstadisticasService;
import com.uamishop.catalogo.shared.event.ProductoAgregadoAlCarritoEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductoAgregadoAlCarritoListener {

    private final ProductoEstadisticasService estadisticasService;

    public ProductoAgregadoAlCarritoListener(ProductoEstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProductoAgregadoAlCarrito(ProductoAgregadoAlCarritoEvent event) {
        estadisticasService.registrarAgregadoAlCarrito(event.productoId());
    }
}