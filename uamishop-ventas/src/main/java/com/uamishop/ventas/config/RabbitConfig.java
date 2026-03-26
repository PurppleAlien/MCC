package com.uamishop.ventas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EVENTS_EXCHANGE = "uamishop.events";
    public static final String QUEUE_VENTAS_LIMPIAR_CARRITO = "ventas.limpiar-carrito";
    public static final String RK_ORDEN_CREADA = "orden.creada";
    public static final String RK_PRODUCTO_AGREGADO = "producto.agregado-carrito";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE);
    }

    @Bean
    public Queue ventasLimpiarCarritoQueue() {
        return new Queue(QUEUE_VENTAS_LIMPIAR_CARRITO, true);
    }

    @Bean
    public Binding ventasLimpiarCarritoBinding(Queue ventasLimpiarCarritoQueue,
                                               TopicExchange eventsExchange) {
        return BindingBuilder.bind(ventasLimpiarCarritoQueue)
                .to(eventsExchange)
                .with(RK_ORDEN_CREADA);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}