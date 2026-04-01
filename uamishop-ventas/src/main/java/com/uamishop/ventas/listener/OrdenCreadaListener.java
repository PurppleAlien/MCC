package com.uamishop.ventas.listener;

import com.uamishop.ventas.config.RabbitConfig;
import com.uamishop.ventas.shared.event.OrdenCreadaEvent;
import com.uamishop.ventas.domain.CarritoId;
import com.uamishop.ventas.service.CarritoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrdenCreadaListener {

    private final CarritoService carritoService;

    public OrdenCreadaListener(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_VENTAS_LIMPIAR_CARRITO)
    @Transactional
    public void onOrdenCreada(OrdenCreadaEvent event) {
        if (event.carritoId() != null) {
            carritoService.completarCheckout(new CarritoId(event.carritoId()));
        }
    }
}