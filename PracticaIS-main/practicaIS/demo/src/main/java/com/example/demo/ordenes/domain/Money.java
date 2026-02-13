package com.example.demo.ordenes.domain;

import java.math.BigDecimal;
import java.util.Objects;
import jakarta.persistence.Embeddable;

/**
 * Valores monetarios inmutables para subtotal, descuento y total.
 * Preserva los montos exactos de la transacción.
 */
@Embeddable
public final class Money {

    private final BigDecimal cantidad;
    private final String moneda;

    /**
     * Constructor protegido para frameworks de persistencia.
     */
    protected Money() {
        this.cantidad = null;
        this.moneda = null;
    }

    /**
     * Crea una nueva instancia de Money.
     * 
     * @param cantidad La cantidad numérica.
     * @param moneda   El código de la moneda (ej. "MXN").
     * @throws IllegalArgumentException si la cantidad o moneda son nulas.
     */
    public Money(BigDecimal cantidad, String moneda) {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad no puede ser nula");
        }
        if (moneda == null || moneda.trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
        this.cantidad = cantidad;
        this.moneda = moneda;
    }

    /**
     * Crea un objeto Money en pesos mexicanos (MXN).
     * 
     * @param cantidad Cantidad numérica.
     * @return Nueva instancia de Money.
     */
    public static Money pesos(double cantidad) {
        return new Money(BigDecimal.valueOf(cantidad), "MXN");
    }

    public Money sumar(Money otro) {
        validarMismaMoneda(otro);
        return new Money(this.cantidad.add(otro.cantidad), this.moneda);
    }

    public Money restar(Money otro) {
        validarMismaMoneda(otro);
        return new Money(this.cantidad.subtract(otro.cantidad), this.moneda);
    }

    public Money multiplicar(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("El factor no puede ser negativo");
        }
        return new Money(this.cantidad.multiply(BigDecimal.valueOf(factor)), this.moneda);
    }

    private void validarMismaMoneda(Money otro) {
        if (otro == null || !this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("Las monedas deben ser iguales para operar");
        }
    }

    // Getters
    public BigDecimal getCantidad() {
        return cantidad;
    }

    public String getMoneda() {
        return moneda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Money money = (Money) o;
        return Objects.equals(cantidad, money.cantidad) &&
                Objects.equals(moneda, money.moneda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cantidad, moneda);
    }

    @Override
    public String toString() {
        return cantidad + " " + moneda;
    }
}
