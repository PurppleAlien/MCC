package com.example.demo.ordenes.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.example.demo.config.RabbitConfig;
import com.example.demo.shared.event.ProductoCompradoEvent;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.demo.catalogo.api.CatalogoApi;
import com.example.demo.catalogo.api.ProductoResumen;
import com.example.demo.ordenes.domain.*;
import com.example.demo.ordenes.dto.*;
import com.example.demo.ordenes.repository.OrdenJpaRepository;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId;
import com.example.demo.shared.event.OrdenCreadaEvent;
import com.example.demo.shared.event.ProductoCompradoEvent;
import com.example.demo.shared.event.ProductoCompradoEvent.ItemComprado;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.ventas.domain.CarritoId;
import com.example.demo.ventas.service.CarritoService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdenService {

    private final OrdenJpaRepository ordenRepository;
    private final CarritoService carritoService;
    private final CatalogoApi catalogoApi;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;

    public OrdenService(OrdenJpaRepository ordenRepository,
                        CarritoService carritoService,
                        CatalogoApi catalogoApi,
                        ApplicationEventPublisher eventPublisher,
                        RabbitTemplate rabbitTemplate) {
        this.ordenRepository = ordenRepository;
        this.carritoService = carritoService;
        this.catalogoApi = catalogoApi;
        this.eventPublisher = eventPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public OrdenResponse crear(OrdenRequest request) {
        List<ItemOrden> items = request.getItems().stream()
                .map(itemReq -> new ItemOrden(
                        ItemOrdenId.generar(),
                        itemReq.getProductoId(),
                        itemReq.getNombreProducto(),
                        itemReq.getSku(),
                        itemReq.getCantidad(),
                        itemReq.getPrecioUnitario()
                ))
                .collect(Collectors.toList());

        Orden orden = Orden.crear(
                OrdenId.generar(),
                request.getNumeroOrden(),
                request.getClienteId(),
                items,
                request.getDireccionEnvio(),
                request.getDescuento() != null ? request.getDescuento() : Money.pesos(0)
        );
        orden = ordenRepository.save(orden);

        // *** NO publicar evento aquí, se publicará cuando se procese el pago ***
        // rabbitTemplate.convertAndSend(...);  // Comentado

        return OrdenResponse.fromOrden(orden);
    }

    @Transactional
    public OrdenResponse crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccion) {
        var carrito = carritoService.obtenerCarrito(carritoId);
        List<ItemOrden> items = carrito.getItems().stream()
                .map(itemCarrito -> {
                    ProductoResumen producto = catalogoApi.obtenerProducto(itemCarrito.getProductoId());
                    return new ItemOrden(
                            ItemOrdenId.generar(),
                            itemCarrito.getProductoId(),
                            producto.nombre(),
                            producto.sku(),
                            itemCarrito.getCantidad(),
                            producto.precio()
                    );
                })
                .collect(Collectors.toList());

        Orden orden = Orden.crear(
                OrdenId.generar(),
                "ORD-" + System.currentTimeMillis(),
                new ClienteId(carritoId.getValor()),
                items,
                direccion,
                Money.pesos(0)
        );
        orden = ordenRepository.save(orden);

        // *** NO publicar evento aquí, se publicará cuando se procese el pago ***
        // rabbitTemplate.convertAndSend(...);  // Comentado

        // Publicar solo el evento de orden creada (si es necesario)
        OrdenCreadaEvent ordenEvent = new OrdenCreadaEvent(
                UUID.randomUUID(),
                Instant.now(),
                orden.getId().getValue(),
                carritoId.getValor(),
                orden.getClienteId().getValue()
        );
        eventPublisher.publishEvent(ordenEvent);
        rabbitTemplate.convertAndSend(
                RabbitConfig.EVENTS_EXCHANGE,
                "orden.creada",
                ordenEvent
        );

        return OrdenResponse.fromOrden(orden);
    }

    @Transactional(readOnly = true)
    public OrdenResponse buscarPorId(OrdenId id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        return OrdenResponse.fromOrden(orden);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> buscarTodas() {
        return ordenRepository.findAll().stream()
                .map(OrdenResponse::fromOrden)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrdenResponse confirmar(OrdenId id, String usuario) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.confirmar(usuario);
        orden = ordenRepository.save(orden);
        return OrdenResponse.fromOrden(orden);
    }

    @Transactional
    public OrdenResponse procesarPago(OrdenId id, String referenciaPago) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.procesarPago("TARJETA", referenciaPago, "sistema");
        orden = ordenRepository.save(orden);

        // --- Publicar evento ProductoCompradoEvent después de procesar el pago ---
        List<ItemComprado> itemsComprados = orden.getItems().stream()
                .map(item -> new ItemComprado(
                        item.getProductoId().getValue(),
                        item.getSku(),
                        item.getCantidad(),
                        item.getPrecioUnitario().getCantidad(),
                        item.getPrecioUnitario().getMoneda()
                ))
                .collect(Collectors.toList());

        ProductoCompradoEvent evento = new ProductoCompradoEvent(
                UUID.randomUUID(),
                Instant.now(),
                orden.getId().getValue(),
                orden.getClienteId().getValue(),
                itemsComprados
        );

        // Publicar mediante RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitConfig.EVENTS_EXCHANGE,
                RabbitConfig.RK_PRODUCTO_COMPRADO,
                evento
        );

        // Opcional: publicar también como evento de aplicación (si hay listeners locales)
        eventPublisher.publishEvent(evento);

        return OrdenResponse.fromOrden(orden);
    }

    @Transactional
    public OrdenResponse marcarEnProceso(OrdenId id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.marcarEnProceso("sistema");
        orden = ordenRepository.save(orden);
        return OrdenResponse.fromOrden(orden);
    }

    @Transactional
    public OrdenResponse marcarEnviada(OrdenId id, InfoEnvio infoEnvio) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.marcarEnviada(infoEnvio, "logistica");
        orden = ordenRepository.save(orden);
        return OrdenResponse.fromOrden(orden);
    }

    @Transactional
    public OrdenResponse marcarEntregada(OrdenId id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.marcarEntregada("repartidor");
        orden = ordenRepository.save(orden);
        return OrdenResponse.fromOrden(orden);
    }

    @Transactional
    public OrdenResponse cancelar(OrdenId id, String motivo) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.cancelar(motivo, "admin");
        orden = ordenRepository.save(orden);
        return OrdenResponse.fromOrden(orden);
    }
}