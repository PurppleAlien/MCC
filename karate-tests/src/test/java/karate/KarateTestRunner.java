package karate;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Test;

class KarateTestRunner {

    @Karate.Test
    Karate testCatalogo() {
        return Karate.run("classpath:karate/catalogo.feature")
            .relativeTo(getClass());
    }

    @Karate.Test
    Karate testCarrito() {
        return Karate.run("classpath:karate/carrito.feature")
            .relativeTo(getClass());
    }

    @Karate.Test
    Karate testOrdenes() {
        return Karate.run("classpath:karate/ordenes.feature")
            .relativeTo(getClass());
    }

    @Karate.Test
    Karate testCircuitBreaker() {
        return Karate.run("classpath:karate/circuit-breaker.feature")
            .relativeTo(getClass());
    }

    @Test
    void testAll() {
        com.intuit.karate.Results results = com.intuit.karate.Runner.path("classpath:karate")
            .outputCucumberJson(true)
            .parallel(4);
        org.junit.jupiter.api.Assertions.assertEquals(0, results.getFailCount(),
            results.getErrorMessages());
    }
}
