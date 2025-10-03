package com.back.pedidos.services;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public interface StatusService {

    void atualizarStatus(UUID id, String status);

    String getStatus(UUID id);

    boolean jaProcessadoComSucesso(UUID id);
}