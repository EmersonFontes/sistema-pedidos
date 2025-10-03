package com.back.pedidos.services;

import com.back.pedidos.exceptions.BusinessException;
import com.back.pedidos.exceptions.InfrastructureException;
import com.back.pedidos.models.Pedido;

public interface ProcessadorPedidoService {

    public void processar(Pedido pedido)throws BusinessException, InfrastructureException;
}
