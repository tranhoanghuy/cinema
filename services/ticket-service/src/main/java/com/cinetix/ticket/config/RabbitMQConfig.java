package com.cinetix.ticket.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE       = "cinetix.events";
    public static final String DLX            = "cinetix.dlx";
    public static final String BOOKING_QUEUE  = "ticket-svc.booking.events";
    public static final String BOOKING_DLQ    = "ticket-svc.booking.events.dlq";
    public static final String BOOKING_BIND   = "booking.events.#";

    @Bean TopicExchange cinetixExchange() { return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build(); }
    @Bean TopicExchange dlxExchange()     { return ExchangeBuilder.topicExchange(DLX).durable(true).build(); }

    @Bean
    Queue bookingEventsQueue() {
        return QueueBuilder.durable(BOOKING_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX)
            .withArgument("x-dead-letter-routing-key", BOOKING_DLQ)
            .build();
    }

    @Bean Queue bookingEventsDlq() { return QueueBuilder.durable(BOOKING_DLQ).build(); }

    @Bean
    Binding bookingBinding(Queue bookingEventsQueue, TopicExchange cinetixExchange) {
        return BindingBuilder.bind(bookingEventsQueue).to(cinetixExchange).with(BOOKING_BIND);
    }

    @Bean Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        var t = new RabbitTemplate(cf); t.setMessageConverter(messageConverter()); return t;
    }
}
