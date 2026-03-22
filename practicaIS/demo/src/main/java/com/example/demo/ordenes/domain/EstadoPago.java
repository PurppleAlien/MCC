package com.example.demo.ordenes.domain;

/**
 * Estados del pago asociado a la orden, sincronizado con el contexto de Pagos.
 */
public enum EstadoPago {
    PENDIENTE,
    PROCESANDO,
    APROBADO,
    RECHAZADO
}
