package com.back.pedidos.services.impl;

import com.back.pedidos.models.StatusPedido;
import com.back.pedidos.services.NotificacaoStatusService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificacaoStatusServiceImpl implements NotificacaoStatusService {

    private final RabbitTemplate rabbitTemplate;
    private final String pedidosExchange;
    private final String sucessoQueue;
    private final String falhaQueue;

    public NotificacaoStatusServiceImpl(RabbitTemplate rabbitTemplate,
                                    @Value("${app.rabbitmq.exchange.pedidos}") String pedidosExchange,
                                    @Value("${app.rabbitmq.queue.status-sucesso}") String sucessoQueue,
                                    @Value("${app.rabbitmq.queue.status-falha}") String falhaQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.pedidosExchange = pedidosExchange;
        this.sucessoQueue = sucessoQueue;
        this.falhaQueue = falhaQueue;
    }

    @Override
    public void notificarSucesso(UUID pedidoId) {
        StatusPedido status = new StatusPedido(pedidoId, StatusPedido.Status.SUCESSO, LocalDateTime.now(), null);
        rabbitTemplate.convertAndSend(pedidosExchange, sucessoQueue, status);
    }

    @Override
    public void notificarFalha(UUID pedidoId, String erro) {
        StatusPedido status = new StatusPedido(pedidoId, StatusPedido.Status.FALHA, LocalDateTime.now(), erro);
        rabbitTemplate.convertAndSend(pedidosExchange, falhaQueue, status);
    }
}
