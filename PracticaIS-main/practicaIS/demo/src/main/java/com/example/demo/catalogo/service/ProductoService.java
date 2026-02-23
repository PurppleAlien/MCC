package com.example.demo.catalogo.service;

import com.example.demo.catalogo.domain.*;
import com.example.demo.catalogo.dto.*;
import com.example.demo.catalogo.repository.CategoriaJpaRepository;
import com.example.demo.catalogo.repository.ProductoJpaRepository;
import com.example.demo.shared.exception.RecursoNoEncontradoException;
import com.example.demo.shared.domain.Money;
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
                request.getSku(),  // Usamos el SKU enviado en la petición
                categoriaId
        );
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

        // Actualizar campos básicos
        producto.actualizarNombreDescripcion(request.getNombre(), request.getDescripcion());

        // Actualizar precio si viene
        if (request.getPrecio() != null) {
            Money nuevoPrecio = new Money(request.getPrecio(), request.getMoneda() != null ? request.getMoneda() : "MXN");
            producto.CambiarPrecio(nuevoPrecio);
        }

        // Actualizar stock si viene
        if (request.getStock() != null) {
            producto.setStock(request.getStock());
        }

        // Actualizar SKU si viene (opcional, pero recomendado)
        if (request.getSku() != null) {
            producto.setSku(request.getSku());
        }

        // Actualizar categoría si viene y es diferente
        if (request.getCategoriaId() != null) {
            CategoriaId nuevaCategoriaId = new CategoriaId(request.getCategoriaId());
            if (!nuevaCategoriaId.equals(producto.getCategoriaId())) {
                categoriaRepository.findById(nuevaCategoriaId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Categoría", request.getCategoriaId()));
                producto.asignarCategoria(nuevaCategoriaId);
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