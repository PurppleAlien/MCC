package com.example.demo.shared.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void deberiaCrearMoneyConValoresValidos() {
        Money money = new Money(BigDecimal.TEN, "MXN");
        assertEquals(BigDecimal.TEN, money.getCantidad());
        assertEquals("MXN", money.getMoneda());
    }

    @Test
    void deberiaCrearMoneyConMetodoPesos() {
        Money money = Money.pesos(100.50);
        assertEquals(BigDecimal.valueOf(100.50), money.getCantidad());
        assertEquals("MXN", money.getMoneda());
    }

    @Test
    void deberiaLanzarExcepcionSiCantidadEsNula() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Money(null, "MXN"));
        assertEquals("La cantidad no puede ser nula", exception.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionSiMonedaEsNula() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Money(BigDecimal.TEN, null));
        assertEquals("La moneda no puede estar vacía", exception.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionSiMonedaEsVacia() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Money(BigDecimal.TEN, ""));
        assertEquals("La moneda no puede estar vacía", exception.getMessage());
    }

    @Test
    void deberiaSumarCorrectamente() {
        Money a = Money.pesos(100);
        Money b = Money.pesos(50);
        Money resultado = a.sumar(b);
        assertEquals(0, BigDecimal.valueOf(150).compareTo(resultado.getCantidad()), 
                "El valor sumado debe ser 150");
        assertEquals("MXN", resultado.getMoneda());
    }

    @Test
    void deberiaLanzarExcepcionAlSumarMonedasDistintas() {
        Money a = new Money(BigDecimal.TEN, "MXN");
        Money b = new Money(BigDecimal.TEN, "USD");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> a.sumar(b));
        assertTrue(exception.getMessage().contains("monedas distintas"));
    }

    @Test
    void deberiaRestarCorrectamente() {
        Money a = Money.pesos(100);
        Money b = Money.pesos(30);
        Money resultado = a.restar(b);
        assertEquals(0, BigDecimal.valueOf(70).compareTo(resultado.getCantidad()),
                "El valor restado debe ser 70");
    }

    @Test
    void deberiaLanzarExcepcionSiRestaDaNegativo() {
        Money a = Money.pesos(30);
        Money b = Money.pesos(100);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> a.restar(b));
        assertEquals("El resultado de la resta no puede ser negativo", exception.getMessage());
    }

    @Test
    void deberiaMultiplicarCorrectamente() {
        Money a = Money.pesos(25);
        Money resultado = a.multiplicar(3);
        assertEquals(0, BigDecimal.valueOf(75).compareTo(resultado.getCantidad()),
                "El valor multiplicado debe ser 75");
    }

    @Test
    void deberiaLanzarExcepcionSiMultiplicadorEsNegativo() {
        Money a = Money.pesos(10);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> a.multiplicar(-1));
        assertEquals("El factor de multiplicación no puede ser negativo", exception.getMessage());
    }

    @Test
    void deberiaSerInmutable() {
        Money original = Money.pesos(100);
        original.sumar(Money.pesos(50));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(original.getCantidad()),
                "El original no debe modificarse");
    }

    @Test
    void deberiaImplementarEqualsYHashCodeCorrectamente() {
        Money a = Money.pesos(100);
        Money b = Money.pesos(100);
        Money c = Money.pesos(200);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void deberiaTenerRepresentacionString() {
        Money money = Money.pesos(123.45);
        assertEquals("123.45 MXN", money.toString());
    }
}