package com.example.demo.ordenes.domain;

/**
 * Define todos los estados posibles de una orden en su ciclo de vida,
 * desde pendiente hasta entregada o cancelada.
 */
public enum EstadoOrden {
    PENDIENTE,
    CONFIRMADA,
    PAGO_PROCESADO,
    EN_PREPARACION,
    ENVIADA,
    EN_TRANSITO,
    ENTREGADA,
    CANCELADA
}
