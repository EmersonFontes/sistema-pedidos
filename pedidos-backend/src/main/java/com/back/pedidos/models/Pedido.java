package com.back.pedidos.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public record Pedido(
        UUID id,
        @NotBlank(message = "Produto não pode ser vazio")
        String produto,
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        int quantidade,
        LocalDateTime dataCriacao
) {}