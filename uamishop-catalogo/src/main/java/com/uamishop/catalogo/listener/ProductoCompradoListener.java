package com.uamishop.catalogo.listener;

import com.uamishop.catalogo.config.RabbitConfig;
import com.uamishop.catalogo.service.ProductoEstadisticasService;
import com.uamishop.catalogo.service.ProductoService;
import com.uamishop.catalogo.shared.event.ProductoCompradoEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductoCompradoListener {

    private final ProductoEstadisticasService estadisticasService;
    private final ProductoService productoService;

    public ProductoCompradoListener(ProductoEstadisticasService estadisticasService,
                                    ProductoService productoService) {
        this.estadisticasService = estadisticasService;
        this.productoService = productoService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CATALOGO_PRODUCTO_COMPRADO)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProductoComprado(ProductoCompradoEvent event) {
        event.items().forEach(item -> {
            estadisticasService.registrarVenta(item.productoId(), item.cantidad());
            productoService.disminuirStock(item.productoId(), item.cantidad());
        });
    }
}