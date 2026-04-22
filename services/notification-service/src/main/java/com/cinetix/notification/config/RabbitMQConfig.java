package com.cinetix.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE   = "cinetix.events";
    public static final String DLX        = "cinetix.dlx";
    public static final String QUEUE      = "notification-svc.events";
    public static final String DLQ        = "notification-svc.events.dlq";
    public static final String BIND       = "booking.events.#";

    @Bean TopicExchange cinetixExchange() { return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build(); }
    @Bean TopicExchange dlxExchange()     { return ExchangeBuilder.topicExchange(DLX).durable(true).build(); }

    @Bean Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE)
            .withArgument("x-dead-letter-exchange", DLX)
            .withArgument("x-dead-letter-routing-key", DLQ).build();
    }

    @Bean Queue notificationDlq() { return QueueBuilder.durable(DLQ).build(); }

    @Bean Binding notificationBinding(Queue notificationQueue, TopicExchange cinetixExchange) {
        return BindingBuilder.bind(notificationQueue).to(cinetixExchange).with(BIND);
    }

    @Bean Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        var t = new RabbitTemplate(cf); t.setMessageConverter(messageConverter()); return t;
    }
}
