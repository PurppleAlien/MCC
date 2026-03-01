package com.example.demo.ordenes.service;

import com.example.demo.catalogo.domain.Producto;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.ordenes.domain.*;
import com.example.demo.ordenes.dto.*;
import com.example.demo.ordenes.repository.OrdenJpaRepository;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.domain.Money;
import com.example.demo.shared.domain.ProductoId; // <-- IMPORTACIÓN AGREGADA
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.ventas.domain.CarritoId;
import com.example.demo.ventas.service.CarritoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdenService {

    private final OrdenJpaRepository ordenRepository;
    private final CarritoService carritoService;
    private final ProductoJpaRepository productoRepository;

    public OrdenService(OrdenJpaRepository ordenRepository, CarritoService carritoService, ProductoJpaRepository productoRepository) {
        this.ordenRepository = ordenRepository;
        this.carritoService = carritoService;
        this.productoRepository = productoRepository;
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
        return OrdenResponse.fromOrden(orden);
    }

    @Transactional
    public OrdenResponse crearDesdeCarrito(CarritoId carritoId, DireccionEnvio direccion) {
        var carrito = carritoService.obtenerCarrito(carritoId);
        List<ItemOrden> items = carrito.getItems().stream()
                .map(itemCarrito -> {
                    Producto producto = productoRepository.findById(itemCarrito.getProductoId())
                            .orElseThrow(() -> new RecursoNoEncontradoException("Producto", itemCarrito.getProductoId().getValue()));
                    return new ItemOrden(
                            ItemOrdenId.generar(),
                            itemCarrito.getProductoId(),
                            producto.getNombre(),
                            producto.getSku(),
                            itemCarrito.getCantidad(),
                            itemCarrito.getPrecioUnitario()
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
        carritoService.completarCheckout(carritoId);
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