package com.back.pedidos.controllers;

import com.back.pedidos.models.Pedido;
import com.back.pedidos.services.PedidoService;
import com.back.pedidos.services.StatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final StatusService statusService;

    public PedidoController(PedidoService pedidoService, StatusService statusService) {
        this.pedidoService = pedidoService;
        this.statusService = statusService;
    }

    @PostMapping
    public ResponseEntity<String> criarPedido(@Valid @RequestBody Pedido pedidoRequest) {

        Pedido novoPedido = new Pedido(
                UUID.randomUUID(),
                pedidoRequest.produto(),
                pedidoRequest.quantidade(),
                LocalDateTime.now()
        );

        pedidoService.enviarPedido(novoPedido);

        return ResponseEntity.accepted().body("{\"id\": \"" + novoPedido.id() + "\"}");
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<String> getStatus(@PathVariable UUID id) {
        String status = statusService.getStatus(id);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("{\"status\": \"" + status + "\"}");
    }
}