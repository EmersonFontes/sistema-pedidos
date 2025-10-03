package com.back.pedidos.services.impl;

import com.back.pedidos.exceptions.BusinessException;
import com.back.pedidos.exceptions.InfrastructureException;
import com.back.pedidos.models.Pedido;
import com.back.pedidos.services.ProcessadorPedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProcessadorPedidoServiceImpl implements ProcessadorPedidoService {

    private static final Logger log = LoggerFactory.getLogger(ProcessadorPedidoServiceImpl.class);

    @Override
    public void processar(Pedido pedido) throws BusinessException, InfrastructureException {
        log.info("Iniciando lógica de negócio para o pedido: {}", pedido.id());

        try {
            // 1. Simula tempo de processamento
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3001));

            // 2. Simula falha de NEGÓCIO
            if (Math.random() < 0.2) {

                throw new BusinessException("Falha aleatória na regra de negócio do pedido.");
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new InfrastructureException("Processamento interrompido por uma thread externa.", e);
        }

        log.info("Lógica de negócio para o pedido {} concluída com SUCESSO.", pedido.id());
    }
}
