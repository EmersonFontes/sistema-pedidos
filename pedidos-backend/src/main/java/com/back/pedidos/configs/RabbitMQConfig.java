package com.back.pedidos.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.pedidos}")
    private String pedidosExchange;

    @Value("${app.rabbitmq.queue.entrada}")
    private String entradaQueue;

    @Value("${app.rabbitmq.queue.entrada-dlq}")
    private String entradaDlq;

    @Value("${app.rabbitmq.queue.status-sucesso}")
    private String sucessoQueue;

    @Value("${app.rabbitmq.queue.status-falha}")
    private String falhaQueue;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(pedidosExchange);
    }

    @Bean
    public Queue entradaQueue() {

        // Configura a fila de entrada para enviar mensagens mortas para a exchange padrão
        return QueueBuilder.durable(entradaQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", entradaDlq)
                .build();
    }

    @Bean
    public Queue entradaDlq() {
        return new Queue(entradaDlq, true);
    }

    @Bean
    public Queue sucessoQueue() {
        return new Queue(sucessoQueue, true);
    }

    @Bean
    public Queue falhaQueue() {
        return new Queue(falhaQueue, true);
    }

    // Bindings
    @Bean
    public Binding bindingEntrada(DirectExchange exchange, Queue entradaQueue) {
        return BindingBuilder.bind(entradaQueue).to(exchange).with(entradaQueue.getName());
    }

    @Bean
    public Binding bindingSucesso(DirectExchange exchange, Queue sucessoQueue) {
        return BindingBuilder.bind(sucessoQueue).to(exchange).with(sucessoQueue.getName());
    }

    @Bean
    public Binding bindingFalha(DirectExchange exchange, Queue falhaQueue) {
        return BindingBuilder.bind(falhaQueue).to(exchange).with(falhaQueue.getName());
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //para poder usar o conversor JSON
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
