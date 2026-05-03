package com.cinetix.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE         = "cinetix.events";
    public static final String DLX              = "cinetix.dlx";
    public static final String PAYMENT_QUEUE    = "booking-svc.payment.events";
    public static final String PAYMENT_DLQ      = "booking-svc.payment.events.dlq";
    public static final String PAYMENT_BINDING  = "payment.events.#";

    @Bean
    public TopicExchange cinetixExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(DLX).durable(true).build();
    }

    @Bean
    public Queue paymentEventsQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX)
            .withArgument("x-dead-letter-routing-key", PAYMENT_DLQ)
            .withArgument("x-message-ttl", 1_800_000)
            .build();
    }

    @Bean
    public Queue paymentEventsDlq() {
        return QueueBuilder.durable(PAYMENT_DLQ).build();
    }

    @Bean
    public Binding paymentEventsBinding() {
        return BindingBuilder.bind(paymentEventsQueue())
            .to(cinetixExchange())
            .with(PAYMENT_BINDING);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        var template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
