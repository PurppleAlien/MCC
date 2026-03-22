package com.example.demo.shared.event;

import java.time.Instant;
import java.util.UUID;

public record OrdenCreadaEvent(
    UUID eventId,
    Instant occurredAt,
    UUID ordenId,
    UUID carritoId,
    UUID clienteId
    
) {}
