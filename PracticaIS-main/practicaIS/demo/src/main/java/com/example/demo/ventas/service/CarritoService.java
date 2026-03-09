package com.example.demo.ventas.service;

import com.example.demo.catalogo.api.CatalogoApi;
import com.example.demo.catalogo.api.ProductoResumen;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.event.ProductoAgregadoAlCarritoEvent;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.shared.exception.StockInsuficienteException;
import com.example.demo.ventas.domain.*;
import com.example.demo.ventas.repository.CarritoRepository;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CatalogoApi catalogoApi;
    private final ApplicationEventPublisher eventPublisher;

    public CarritoService(CarritoRepository carritoRepository, CatalogoApi catalogoApi,ApplicationEventPublisher eventPublisher) {
        this.carritoRepository = carritoRepository;
        this.catalogoApi = catalogoApi;
        this.eventPublisher= eventPublisher;
    }

    @Transactional
    public Carrito obtenerOCrear(CarritoId id, ClienteId clienteId) {
        return carritoRepository.findById(id).orElseGet(() -> {
            Carrito nuevo = new Carrito(id, clienteId);
            return carritoRepository.save(nuevo);
        });
    }

    @Transactional
    public Carrito crear(ClienteId clienteId) {
        return obtenerOCrear(CarritoId.generar(), clienteId);
    }

    @Transactional
    public Carrito agregarProducto(CarritoId carritoId, ProductoId productoId, int cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId.getValor()));

        ProductoResumen producto = catalogoApi.obtenerProducto(productoId);

        if (producto.stock() < cantidad) {
            throw new StockInsuficienteException(producto.nombre(), cantidad, producto.stock());
        }

        ProductoRef productoRef = new ProductoRef(productoId, producto.precio());
        carrito.agregarProducto(productoRef, cantidad);
       // return carritoRepository.save(carrito);
       carrito=carritoRepository.save(carrito);
       
       ProductoAgregadoAlCarritoEvent evento = new ProductoAgregadoAlCarritoEvent(
    UUID.randomUUID(),
     Instant.now(),
     producto.id().getValue(),
     carrito.getId().getValor(),
     cantidad,
     producto.precio().getCantidad(),
     producto.precio().getMoneda()   
);
        eventPublisher.publishEvent(evento);
       return carrito;
    }

    @Transactional
    public Carrito modificarCantidad(CarritoId carritoId, ProductoId productoId, int nuevaCantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId.getValor()));
        carrito.modificarCantidad(productoId, nuevaCantidad);
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito eliminarProducto(CarritoId carritoId, ProductoId productoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId.getValor()));
        carrito.eliminarProducto(productoId);
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito vaciar(CarritoId carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId.getValor()));
        carrito.vaciar();
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito iniciarCheckout(CarritoId carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId.getValor()));
        carrito.iniciarCheckout();
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito completarCheckout(CarritoId carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId.getValor()));
        carrito.completarCheckout();
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito abandonar(CarritoId carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId.getValor()));
        carrito.abandonar();
        return carritoRepository.save(carrito);
    }

    @Transactional(readOnly = true)
    public Carrito obtenerCarrito(CarritoId carritoId) {
        return carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito", carritoId.getValor()));
    }
}