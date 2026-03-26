package com.example.demo.ventas.api;

import com.example.demo.shared.domain.ClienteId;
import java.util.List;
import java.util.UUID;

public record CarritoResumen(
    UUID carritoId,
    ClienteId clienteId,
    String estado,
    List<ItemCarritoResumen> items
) {}