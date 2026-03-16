package com.example.demo.ventas.listener;

import com.example.demo.shared.event.OrdenCreadaEvent;
import com.example.demo.ventas.domain.CarritoId;
import com.example.demo.ventas.service.CarritoService;
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