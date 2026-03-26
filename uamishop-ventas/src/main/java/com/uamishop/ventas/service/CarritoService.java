package com.uamishop.ventas.service;

import com.uamishop.ventas.clients.CatalogoApiClient;
import com.uamishop.ventas.clients.CatalogoApiClient.ProductoInfo;
import com.uamishop.ventas.config.RabbitConfig;
import com.uamishop.ventas.domain.*;
import com.uamishop.ventas.repository.CarritoRepository;
import com.uamishop.ventas.shared.domain.ClienteId;
import com.uamishop.ventas.shared.domain.ProductoId;
import com.uamishop.ventas.shared.event.ProductoAgregadoAlCarritoEvent;
import com.uamishop.ventas.shared.exception.RecursoNoEncontradoException;
import com.uamishop.ventas.shared.exception.StockInsuficienteException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CatalogoApiClient catalogoApiClient;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;

    public CarritoService(CarritoRepository carritoRepository,
                          CatalogoApiClient catalogoApiClient,
                          ApplicationEventPublisher eventPublisher,
                          RabbitTemplate rabbitTemplate) {
        this.carritoRepository = carritoRepository;
        this.catalogoApiClient = catalogoApiClient;
        this.eventPublisher = eventPublisher;
        this.rabbitTemplate = rabbitTemplate;
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

        // Obtener información del producto desde catálogo
        ProductoInfo producto = catalogoApiClient.obtenerProducto(productoId);

        if (producto.stock() < cantidad) {
            throw new StockInsuficienteException(producto.nombre(), cantidad, producto.stock());
        }

        // Crear referencia de producto con precio
        ProductoRef productoRef = new ProductoRef(productoId, producto.precio());
        carrito.agregarProducto(productoRef, cantidad);
        carrito = carritoRepository.save(carrito);

        // Publicar evento interno
        ProductoAgregadoAlCarritoEvent event = new ProductoAgregadoAlCarritoEvent(
                UUID.randomUUID(),
                Instant.now(),
                productoId.getValue(),
                carritoId.getValor(),
                cantidad,
                producto.precio().getCantidad(),
                producto.precio().getMoneda()
        );
        eventPublisher.publishEvent(event);

        // Publicar evento a RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitConfig.EVENTS_EXCHANGE,
                RabbitConfig.RK_PRODUCTO_AGREGADO,
                event
        );

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