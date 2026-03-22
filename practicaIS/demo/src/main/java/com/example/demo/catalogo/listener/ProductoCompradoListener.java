package com.example.demo.catalogo.listener;

import com.example.demo.catalogo.service.ProductoEstadisticasService;
import com.example.demo.shared.event.ProductoCompradoEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductoCompradoListener {

    private final ProductoEstadisticasService estadisticasService;

    public ProductoCompradoListener(ProductoEstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProductoComprado(ProductoCompradoEvent event) {
        event.items().forEach(item ->
            estadisticasService.registrarVenta(item.productoId(), item.cantidad())
        );
    }
}