package com.fescode.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PedidoPdfDTO {
    private Long idPedido;
    private String fechaPedido;
    private String nombreCliente;
    private String emailCliente;
    private String direccionEnvio;
    private Double total;
    private List<ItemPedidoDTO> items;

    @Data
    public static class ItemPedidoDTO {
        private String nombreProducto;
        private int cantidad;
        private double precio;
    }

}
