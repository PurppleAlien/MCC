package com.example.demo.ventas.service;

import com.example.demo.ventas.api.CarritoResumen;
import com.example.demo.ventas.api.ItemCarritoResumen;
import com.example.demo.ventas.api.VentasApi;
import com.example.demo.ventas.domain.Carrito;
import com.example.demo.ventas.domain.CarritoId;
import com.example.demo.ventas.repository.CarritoRepository;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VentasServiceImpl implements VentasApi {

    private final CarritoRepository carritoRepository;

    public VentasServiceImpl(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CarritoResumen obtenerCarrito(UUID carritoId) {
        Carrito carrito = carritoRepository.findById(new CarritoId(carritoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId));
        return new CarritoResumen(
                carrito.getId().getValor(),
                carrito.getClienteId(),
                carrito.getEstado().name(),
                carrito.getItems().stream()
                        .map(item -> new ItemCarritoResumen(
                                item.getProductoId(),
                                item.getCantidad(),
                                item.getPrecioUnitario()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    @Transactional
    public void completarCheckout(UUID carritoId) {
        Carrito carrito = carritoRepository.findById(new CarritoId(carritoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId));
        carrito.completarCheckout();
        carritoRepository.save(carrito);
    }
}