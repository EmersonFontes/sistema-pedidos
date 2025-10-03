package com.back.pedidos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StatusPedido(
        UUID idPedido,
        Status status,
        LocalDateTime dataProcessamento,
        String mensagemErro
) {
    public enum Status { SUCESSO, FALHA, PROCESSANDO, RECEBIDO }
}