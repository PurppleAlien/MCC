package com.uamishop.ordenes.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uamishop.ordenes.outbox.domain.OutboxEvent;
import com.uamishop.ordenes.outbox.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void guardar(String aggregateType, String aggregateId,
                        String eventType, Object eventPayload, String routingKey) {
        try {
            String payload = objectMapper.writeValueAsString(eventPayload);
            OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                eventType,
                payload,
                routingKey
            );
            outboxEventRepository.save(event);
            log.debug("Evento outbox guardado: type={}, aggregateId={}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando evento para outbox: " + eventType, e);
        }
    }
}
