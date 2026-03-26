package com.uamishop.catalogo.service;

import com.uamishop.catalogo.domain.*;
import com.uamishop.catalogo.dto.*;
import com.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.uamishop.catalogo.repository.ProductoJpaRepository;
import com.uamishop.catalogo.shared.domain.Money;
import com.uamishop.catalogo.shared.domain.ProductoId;
import com.uamishop.catalogo.shared.exception.RecursoNoEncontradoException;
import com.uamishop.catalogo.shared.exception.StockInsuficienteException; // 👈 IMPORTANTE
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoJpaRepository productoRepository;
    private final CategoriaJpaRepository categoriaRepository;

    public ProductoService(ProductoJpaRepository productoRepository, CategoriaJpaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // --- Productos ---
    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {
        CategoriaId categoriaId = new CategoriaId(request.getCategoriaId());
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", request.getCategoriaId()));

        Money precio = new Money(request.getPrecio(), request.getMoneda() != null ? request.getMoneda() : "MXN");

        Producto producto = Producto.crear(
                request.getNombre(),
                request.getDescripcion(),
                precio,
                request.getStock(),
                request.getSku(),
                categoriaId
        );

        // 🔥 AGREGAR IMÁGENES AL CREAR
        if (request.getImagenesUrls() != null) {
            for (String url : request.getImagenesUrls()) {
                producto.agregarImagen(
            new Imagen(
                java.util.UUID.randomUUID(),
                url,
                "imagen producto",
                1
                )
             );
            }
        }

        producto = productoRepository.save(producto);
        return ProductoResponse.fromProducto(producto);
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscarProductoPorId(ProductoId id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id.getValue()));
        return ProductoResponse.fromProducto(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarTodosProductos() {
        return productoRepository.findAll().stream()
                .map(ProductoResponse::fromProducto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoResponse actualizarProducto(ProductoId id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id.getValue()));

        producto.actualizarNombreDescripcion(request.getNombre(), request.getDescripcion());

        if (request.getPrecio() != null) {
            Money nuevoPrecio = new Money(request.getPrecio(), request.getMoneda() != null ? request.getMoneda() : "MXN");
            producto.CambiarPrecio(nuevoPrecio);
        }

        if (request.getStock() != null) {
            producto.setStock(request.getStock());
        }

        if (request.getSku() != null) {
            producto.setSku(request.getSku());
        }

        if (request.getCategoriaId() != null) {
            CategoriaId nuevaCategoriaId = new CategoriaId(request.getCategoriaId());
            if (!nuevaCategoriaId.equals(producto.getCategoriaId())) {
                categoriaRepository.findById(nuevaCategoriaId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", request.getCategoriaId()));
                producto.asignarCategoria(nuevaCategoriaId);
            }
        }

        // 🔥 AGREGAR IMÁGENES AL ACTUALIZAR
        if (request.getImagenesUrls() != null) {
            for (String url : request.getImagenesUrls()) {
                producto.agregarImagen(
                    new Imagen(
                        java.util.UUID.randomUUID(),
                        url,
                        "imagen producto",
                        1
                    )
                );
            }
        }

        producto = productoRepository.save(producto);
        return ProductoResponse.fromProducto(producto);
    }

    @Transactional
    public ProductoResponse activarProducto(ProductoId id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id.getValue()));
        producto.activar();
        producto = productoRepository.save(producto);
        return ProductoResponse.fromProducto(producto);
    }

    @Transactional
    public ProductoResponse desactivarProducto(ProductoId id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id.getValue()));
        producto.desactivar();
        producto = productoRepository.save(producto);
        return ProductoResponse.fromProducto(producto);
    }

    // 👇 NUEVO MÉTODO PARA DISMINUIR STOCK
    @Transactional
    public void disminuirStock(java.util.UUID productoId, int cantidad) {
        Producto producto = productoRepository.findById(new ProductoId(productoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));
        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException("Stock insuficiente para el producto: " + productoId);
        }
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }

    // --- Categorías ---
    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        CategoriaId id = CategoriaId.generar();
        Categoria categoria = new Categoria(
                id,
                request.getNombre(),
                request.getDescripcion(),
                request.getCategoriaPadreId() != null ? new CategoriaId(request.getCategoriaPadreId()) : null
        );
        categoria = categoriaRepository.save(categoria);
        return CategoriaResponse.fromCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarCategoriaPorId(CategoriaId id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id.id()));
        return CategoriaResponse.fromCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> buscarTodasCategorias() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaResponse::fromCategoria)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaResponse actualizarCategoria(CategoriaId id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id.id()));
        categoria.actualizar(request.getNombre(), request.getDescripcion());
        if (request.getCategoriaPadreId() != null) {
            categoria.AsignarPadre(new CategoriaId(request.getCategoriaPadreId()));
        }
        categoria = categoriaRepository.save(categoria);
        return CategoriaResponse.fromCategoria(categoria);
    }
}