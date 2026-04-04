package com.uamishop.ordenes.outbox.service;

import com.uamishop.ordenes.config.RabbitConfig;
import com.uamishop.ordenes.outbox.domain.OutboxEvent;
import com.uamishop.ordenes.outbox.domain.OutboxStatus;
import com.uamishop.ordenes.outbox.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                           RabbitTemplate rabbitTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publicarEventosPendientes() {
        List<OutboxEvent> pendientes = outboxEventRepository
            .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDIENTE);

        for (OutboxEvent evento : pendientes) {
            try {
                MessageProperties props = new MessageProperties();
                props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                props.setHeader("__TypeId__", evento.getEventType());
                Message message = new Message(
                    evento.getPayload().getBytes(StandardCharsets.UTF_8), props);

                rabbitTemplate.send(RabbitConfig.EVENTS_EXCHANGE, evento.getRoutingKey(), message);
                evento.markPublished();
                outboxEventRepository.save(evento);
                log.info("Evento outbox publicado: id={}, type={}, routingKey={}",
                    evento.getId(), evento.getEventType(), evento.getRoutingKey());
            } catch (Exception e) {
                log.error("Error publicando evento outbox id={}: {}", evento.getId(), e.getMessage());
                evento.markFailed();
                outboxEventRepository.save(evento);
            }
        }
    }
}
