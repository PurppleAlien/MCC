package com.example.demo.catalogo.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

//import java.math.BigDecimal;


public class Productotest {
    @Test
    void CrearProductoCorrecto() {
        String nombre = "Laptop";
        String descripcion = "Laptop de alto rendimiento";
        Money precio = Money.pesos(25000);
        CategoriaId categoriaId = CategoriaId.generar();

        Producto producto = Producto.crear(nombre, descripcion, precio, categoriaId);

        assertNotNull(producto);
        assertNotNull(producto.getId());
        assertEquals(nombre, producto.getNombre());
        assertEquals(precio, producto.getPrecio());
        assertTrue(producto.getDisponible());
        assertNotNull(producto.getFechaCreacion());
    }

@Test
 void CrearProductoConNombreMuyCorto() {
        String nombre = "La";
        String descripcion = "Descripcion correcta";
        Money precio = Money.pesos(1000);
        CategoriaId categoriaId = CategoriaId.generar();

        assertThrows(IllegalArgumentException.class, () ->
                Producto.crear(nombre, descripcion, precio, categoriaId)
        );
    }
      @Test
    void CrearProductoConPrecioCero() {
        String nombre = "Laptop";
        String descripcion = "Descripcion correcta";
        Money precio = Money.pesos(0);
        CategoriaId categoriaId = CategoriaId.generar();

        assertThrows(IllegalArgumentException.class, () ->
                Producto.crear(nombre, descripcion, precio, categoriaId)
        );
    }
 @Test
    void CambiarPrecioCorrecto() {
        String nombre = "Laptop";
        String descripcion = "Laptop de alto rendimiento";
        Money precioInicial = Money.pesos(10000);
        CategoriaId categoriaId = CategoriaId.generar();

        Producto producto = Producto.crear(nombre, descripcion, precioInicial, categoriaId);

        Money nuevoPrecio = Money.pesos(14000);
        producto.CambiarPrecio(nuevoPrecio);

        assertEquals(nuevoPrecio, producto.getPrecio());
    }

    @Test
    void CambiarPrecioANegativo() {
        String nombre = "Laptop";
        String descripcion = "Laptop de alto rendimiento";
        Money precioInicial = Money.pesos(10000);
        CategoriaId categoriaId = CategoriaId.generar();

        Producto producto = Producto.crear(nombre, descripcion, precioInicial, categoriaId);

        Money nuevoPrecio = Money.pesos(-100);

        assertThrows(IllegalArgumentException.class, () ->
                producto.CambiarPrecio(nuevoPrecio)
        );
    }


    @Test
    void DesactivarProducto() {
        Producto producto = Producto.crear(
                "Laptop",
                "Laptop de alto rendimiento",
                Money.pesos(25000),
                CategoriaId.generar()
        );

        producto.desactivar();

        assertFalse(producto.getDisponible());
    }

    @Test
    void DesactivarProductoDosVeces() {
        Producto producto = Producto.crear(
                "Laptop",
                "Laptop de alto rendimiento",
                Money.pesos(25000),
                CategoriaId.generar()
        );

        producto.desactivar();

        assertThrows(IllegalStateException.class, () ->
                producto.desactivar()
        );
    }
            
   

    @Test
    void noDebeActivarProductoSinImagenes() {
        Producto producto = Producto.crear(
                "Laptop",
                "Laptop de alto rendimiento",
                Money.pesos(25000),
                CategoriaId.generar()
        );

        assertThrows(IllegalStateException.class, () ->
                producto.activar()
        );
    }
   @Test
void agregarImagenCorrectamente() {
    Producto producto = Producto.crear(
            "Laptop",
            "Laptop de alto rendimiento",
            Money.pesos(25000),
            CategoriaId.generar()
    );

    Imagen imagen = Imagen.generar();

    producto.agregarImagen(imagen);

    assertEquals(1, producto.getImagenes().size());
}

@Test
void agregarmasDeCincoImagenes() {
    Producto producto = Producto.crear(
            "Laptop",
            "Laptop de alto rendimiento",
            Money.pesos(25000),
            CategoriaId.generar()
    );
    for (int i = 0; i < 5; i++) {
        producto.agregarImagen(Imagen.generar());
    }
    Imagen imagenExtra = Imagen.generar();
    assertThrows(IllegalArgumentException.class, () ->
            producto.agregarImagen(imagenExtra)
    );

} 
}