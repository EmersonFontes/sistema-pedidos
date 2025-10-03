package com.back.pedidos.services.impl;

import com.back.pedidos.models.StatusPedido;
import com.back.pedidos.services.StatusService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatusServiceImpl implements StatusService {

    // Usar ConcurrentHashMap para segurança em ambiente multi-thread
    private final Map<UUID, String> statusPedidos = new ConcurrentHashMap<>();

    public void atualizarStatus(UUID id, String status) {
        statusPedidos.put(id, status);
    }

    public String getStatus(UUID id) {
        return statusPedidos.get(id);
    }

    public boolean jaProcessadoComSucesso(UUID id) {
        String status = statusPedidos.get(id); // Supondo que statusPedidos é seu Map<UUID, String>
        return StatusPedido.Status.SUCESSO.name().equals(status);
    }
}
