package com.back.pedidos.consumers;

import com.back.pedidos.exceptions.BusinessException;
import com.back.pedidos.exceptions.InfrastructureException;
import com.back.pedidos.models.Pedido;
import com.back.pedidos.models.StatusPedido;
import com.back.pedidos.services.NotificacaoStatusService;
import com.back.pedidos.services.ProcessadorPedidoService;
import com.back.pedidos.services.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PedidoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoConsumer.class);

    private final ProcessadorPedidoService processadorService;
    private final NotificacaoStatusService notificacaoService;
    private final StatusService statusService;

    public PedidoConsumer(ProcessadorPedidoService processadorService,
                          NotificacaoStatusService notificacaoService,
                          StatusService statusService) {
        this.processadorService = processadorService;
        this.notificacaoService = notificacaoService;
        this.statusService = statusService;
    }

    @RabbitListener(queues = {"${app.rabbitmq.queue.entrada}"})
    public void processarPedido(@Payload Pedido pedido) {
        log.info("Mensagem recebida para o pedido: {}", pedido.id());

        // Verificação de idempotência
        if (statusService.jaProcessadoComSucesso(pedido.id())) {
            log.warn("Pedido {} já foi processado com sucesso. Ignorando mensagem duplicada.", pedido.id());
            return;
        }

        statusService.atualizarStatus(pedido.id(), StatusPedido.Status.PROCESSANDO.name());

        try {
            processadorService.processar(pedido);

            statusService.atualizarStatus(pedido.id(), StatusPedido.Status.SUCESSO.name());
            notificacaoService.notificarSucesso(pedido.id());
            log.info("Orquestração do pedido {} finalizada com SUCESSO.", pedido.id());

        } catch (BusinessException e) {

            log.error("[ERRO DE NEGÓCIO] Pedido {}: {}.", pedido.id(), e.getMessage());
            statusService.atualizarStatus(pedido.id(), StatusPedido.Status.FALHA.name());
            notificacaoService.notificarFalha(pedido.id(), e.getMessage());

            // Rejeita permanentemente a mensagem.
            throw new AmqpRejectAndDontRequeueException(e);

        } catch (InfrastructureException e) {

            log.warn("[ERRO DE INFRA] Pedido {}: {}.", pedido.id(), e.getMessage(), e);
            statusService.atualizarStatus(pedido.id(), StatusPedido.Status.FALHA.name());
            notificacaoService.notificarFalha(pedido.id(), "Erro técnico no processamento.");

            throw new AmqpRejectAndDontRequeueException(e);

        } catch (Exception e) {

            log.error("[ERRO INESPERADO] Pedido {}: Ocorreu um erro não previsto.", pedido.id(), e);
            statusService.atualizarStatus(pedido.id(), StatusPedido.Status.FALHA.name());
            notificacaoService.notificarFalha(pedido.id(), "Erro inesperado no sistema.");

            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    // O listener da DLQ permanece o mesmo
    @RabbitListener(queues = "${app.rabbitmq.queue.entrada-dlq}")
    public void processarDlq(Pedido pedido) {
        log.warn("Mensagem recebida na DLQ: {}. Requer análise manual ou política de retentativa.", pedido);
    }
}