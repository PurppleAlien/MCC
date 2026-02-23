package com.example.demo.ventas.service;

import com.example.demo.catalogo.domain.Producto;
import com.example.demo.catalogo.domain.ProductoId;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.shared.domain.ClienteId;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.shared.exception.StockInsuficienteException;
import com.example.demo.ventas.domain.*;
import com.example.demo.ventas.repository.CarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoJpaRepository productoRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductoJpaRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
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
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId.getValue()));
        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(producto.getNombre(), cantidad, producto.getStock());
        }
        ProductoRef productoRef = new ProductoRef(productoId, producto.getPrecio());
        carrito.agregarProducto(productoRef, cantidad);
        return carritoRepository.save(carrito);
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