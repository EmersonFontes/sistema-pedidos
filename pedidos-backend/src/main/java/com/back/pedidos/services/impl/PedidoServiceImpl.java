package com.back.pedidos.services.impl;

import com.back.pedidos.models.Pedido;
import com.back.pedidos.models.StatusPedido;
import com.back.pedidos.services.PedidoService;
import com.back.pedidos.services.StatusService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final RabbitTemplate rabbitTemplate;
    private final StatusService statusService;
    private final String entradaQueue;
    private final String pedidosExchange;


    public PedidoServiceImpl(RabbitTemplate rabbitTemplate, StatusService statusService,
                         @Value("${app.rabbitmq.queue.entrada}") String entradaQueue,
                         @Value("${app.rabbitmq.exchange.pedidos}") String pedidosExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.statusService = statusService;
        this.entradaQueue = entradaQueue;
        this.pedidosExchange = pedidosExchange;
    }
    @Override
    public void enviarPedido(Pedido pedido) {
        statusService.atualizarStatus(pedido.id(), StatusPedido.Status.RECEBIDO.name());
        rabbitTemplate.convertAndSend(pedidosExchange, entradaQueue, pedido);
    }
}
