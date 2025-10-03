package com.back.pedidos;

import com.back.pedidos.models.Pedido;
import com.back.pedidos.models.StatusPedido;
import com.back.pedidos.services.PedidoService;
import com.back.pedidos.services.StatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PedidosApplicationTests {

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private StatusService statusService;

    // Injeta o bean real do PedidoService que o Spring criou.
    @Autowired
    private PedidoService pedidoService;

    @Value("${app.rabbitmq.exchange.pedidos}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.entrada}")
    private String queueName;

    @Test
    @DisplayName("Deve publicar o pedido no RabbitMQ e atualizar o status para RECEBIDO (com Contexto Spring)")
    void enviarPedido() {

        // Arrange: Cria um objeto de teste para o pedido.
        Pedido pedidoDeTeste = new Pedido(
                UUID.randomUUID(),
                "Produto de Teste com Contexto",
                10,
                LocalDateTime.now()
        );

        pedidoService.enviarPedido(pedidoDeTeste);

        verify(statusService).atualizarStatus(
                eq(pedidoDeTeste.id()),
                eq(StatusPedido.Status.RECEBIDO.name())
        );

        verify(rabbitTemplate).convertAndSend(
                eq(exchangeName),
                eq(queueName),
                eq(pedidoDeTeste)
        );
    }
}