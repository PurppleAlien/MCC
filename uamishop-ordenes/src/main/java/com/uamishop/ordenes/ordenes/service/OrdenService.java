package com.uamishop.ordenes.ordenes.service;

import com.uamishop.ordenes.config.RabbitConfig;
import com.uamishop.ordenes.ordenes.domain.*;
import com.uamishop.ordenes.ordenes.dto.*;
import com.uamishop.ordenes.ordenes.repository.OrdenJpaRepository;
import com.uamishop.ordenes.shared.domain.Money;
import com.uamishop.ordenes.shared.event.OrdenCreadaEvent;
import com.uamishop.ordenes.shared.event.ProductoCompradoEvent;
import com.uamishop.ordenes.shared.event.ProductoCompradoEvent.ItemComprado;
import com.uamishop.ordenes.shared.exception.RecursoNoEncontradoException;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrdenService {

    private final OrdenJpaRepository ordenRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrdenService(OrdenJpaRepository ordenRepository,
                        RabbitTemplate rabbitTemplate) {
        this.ordenRepository = ordenRepository;
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

        // Publicar evento orden.creada para que Ventas complete el checkout
        OrdenCreadaEvent ordenCreadaEvent = new OrdenCreadaEvent(
                UUID.randomUUID(),
                Instant.now(),
                orden.getId().getValue(),
                request.getCarritoId(),
                orden.getClienteId().getValue()
        );
        rabbitTemplate.convertAndSend(
                RabbitConfig.EVENTS_EXCHANGE,
                "orden.creada",
                ordenCreadaEvent
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
        return OrdenResponse.fromOrden(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenResponse procesarPago(OrdenId id, String referenciaPago) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.procesarPago("TARJETA", referenciaPago, "sistema");
        orden = ordenRepository.save(orden);

        // Publicar evento producto.comprado para que Catalogo reduzca stock
        List<ItemComprado> itemsComprados = orden.getItems().stream()
                .map(item -> new ItemComprado(
                        item.getProductoId().getValue(),
                        item.getSku(),
                        item.getCantidad(),
                        item.getPrecioUnitario().getCantidad(),
                        item.getPrecioUnitario().getMoneda()
                ))
                .toList();

        ProductoCompradoEvent event = new ProductoCompradoEvent(
                UUID.randomUUID(),
                Instant.now(),
                orden.getId().getValue(),
                orden.getClienteId().getValue(),
                itemsComprados
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.EVENTS_EXCHANGE,
                RabbitConfig.RK_PRODUCTO_COMPRADO,
                event
        );

        return OrdenResponse.fromOrden(orden);
    }

    @Transactional
    public OrdenResponse marcarEnProceso(OrdenId id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.marcarEnProceso("sistema");
        return OrdenResponse.fromOrden(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenResponse marcarEnviada(OrdenId id, InfoEnvio infoEnvio) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.marcarEnviada(infoEnvio, "logistica");
        return OrdenResponse.fromOrden(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenResponse marcarEntregada(OrdenId id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.marcarEntregada("repartidor");
        return OrdenResponse.fromOrden(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenResponse cancelar(OrdenId id, String motivo) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden", id.getValue()));
        orden.cancelar(motivo, "admin");
        return OrdenResponse.fromOrden(ordenRepository.save(orden));
    }
}
