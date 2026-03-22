package com.example.demo.catalogo.domain;

import jakarta.persistence.Embeddable;
import java.util.UUID;
@Embeddable


public record Imagen(UUID id, String url, String altText, Integer orden) {

    public Imagen {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("La URL de la imagen no puede ser nula o vacía");
        }
        if (altText == null) {
            altText = "";
        }
        if (orden == null || orden < 0) {
            orden = 0;
        }
    }

    public static Imagen generar() {
        return new Imagen(UUID.randomUUID(), "https://default.url/image.png", "Imagen por defecto", 0);
    }


}