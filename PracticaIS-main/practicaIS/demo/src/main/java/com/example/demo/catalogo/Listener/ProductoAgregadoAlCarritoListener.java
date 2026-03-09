package com.example.demo.catalogo.Listener;
import com.example.demo.catalogo.service.ProductoEstadisticasService;
import com.example.demo.shared.event.ProductoAgregadoAlCarritoEvent;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class ProductoAgregadoAlCarritoListener {
 
private final ProductoEstadisticasService estadisticasService;

public ProductoAgregadoAlCarritoListener(ProductoEstadisticasService estadisticasService) {
       this.estadisticasService = estadisticasService;
   }


   @EventListener
   @Async // El listener se ejecuta en un hilo distinto, las métricas son eventualmente consistentes
   @Transactional(propagation = Propagation.REQUIRES_NEW) // Manejamos una transacción distinta (secundaria) pues una falla aquí no debe afectar ni bloquear la transacción principal

    public void onProductoAgregadoalCarrito(ProductoAgregadoAlCarritoEvent event) {
     estadisticasService.RegistrarAgregadosAlCarrito(event.productoId());
    }
}
