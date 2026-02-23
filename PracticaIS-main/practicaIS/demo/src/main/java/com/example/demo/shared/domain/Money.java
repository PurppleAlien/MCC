package com.example.demo.shared.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object inmutable que representa una cantidad monetaria con su moneda.
 */
@Embeddable
public final class Money {

    private final BigDecimal cantidad;
    private final String moneda;

    protected Money() {
        this.cantidad = null;
        this.moneda = null;
    }

    @JsonCreator
    public Money(@JsonProperty("cantidad") BigDecimal cantidad, @JsonProperty("moneda") String moneda) {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad no puede ser nula");
        }
        if (moneda == null || moneda.isBlank()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
        this.cantidad = cantidad;
        this.moneda = moneda;
    }

    public static Money pesos(double cantidad) {
        return new Money(BigDecimal.valueOf(cantidad), "MXN");
    }

    public Money sumar(Money otro) {
        validarMismaMoneda(otro);
        return new Money(this.cantidad.add(otro.cantidad), this.moneda);
    }

    public Money restar(Money otro) {
        validarMismaMoneda(otro);
        BigDecimal resultado = this.cantidad.subtract(otro.cantidad);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El resultado de la resta no puede ser negativo");
        }
        return new Money(resultado, this.moneda);
    }

    public Money multiplicar(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("El factor de multiplicación no puede ser negativo");
        }
        return new Money(this.cantidad.multiply(BigDecimal.valueOf(factor)), this.moneda);
    }

    private void validarMismaMoneda(Money otro) {
        if (otro == null) {
            throw new IllegalArgumentException("El otro Money no puede ser nulo");
        }
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("No se pueden operar monedas distintas: " + this.moneda + " y " + otro.moneda);
        }
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public String getMoneda() {
        return moneda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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