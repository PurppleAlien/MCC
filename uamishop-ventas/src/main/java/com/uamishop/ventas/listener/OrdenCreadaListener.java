package com.uamishop.ventas.listener;

import com.uamishop.ventas.shared.event.OrdenCreadaEvent;
import com.uamishop.ventas.domain.CarritoId;
import com.uamishop.ventas.service.CarritoService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrdenCreadaListener {

    private final CarritoService carritoService;

    public OrdenCreadaListener(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @EventListener
    @Transactional
    public void onOrdenCreada(OrdenCreadaEvent event) {
        carritoService.completarCheckout(new CarritoId(event.carritoId()));
    }
}