package com.example.demo.catalogo.domain;

import java.math.BigDecimal;
import jakarta.persistence.Embeddable;

@Embeddable
public record Money(BigDecimal cantidad, String moneda) {

    public Money {
        if (cantidad == null || moneda == null || moneda.isBlank()) {
            throw new IllegalArgumentException("Cantidad y moneda no pueden ser nulos o vacíos");
        }
    }

    public static Money pesos(double cantidad){
        return new Money(BigDecimal.valueOf(cantidad), "MXN");
    }
      public Money sumar(Money otro) {
        if (otro == null) {
            throw new IllegalArgumentException("No se puede sumar un Money nulo");
        }

        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException(
                "No se pueden sumar monedas distintas: "
                + this.moneda + " y " + otro.moneda
            );
        }

        return new Money(
                this.cantidad.add(otro.cantidad),
                this.moneda
        );
    }
     public Money restar(Money otro) {
        if (otro == null) {
            throw new IllegalArgumentException("No se puede restar un Money nulo");
        }

        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException(
                "No se pueden restar monedas distintas: "
                + this.moneda + " y " + otro.moneda
            );
        }

        return new Money(
                this.cantidad.subtract(otro.cantidad),
                this.moneda
        );
    }
     public Money multiplicar(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("El factor de multiplicación no puede ser negativo");
        }

        return new Money(
                this.cantidad.multiply(BigDecimal.valueOf(factor)),
                this.moneda
        );

    }
}