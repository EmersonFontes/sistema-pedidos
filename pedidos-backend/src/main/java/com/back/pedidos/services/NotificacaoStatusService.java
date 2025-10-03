package com.back.pedidos.services;

import java.util.UUID;

public interface NotificacaoStatusService {

    public void notificarSucesso(UUID pedidoId);

    public void notificarFalha(UUID pedidoId, String erro);
}
