package com.milsabores.ventas.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.milsabores.ventas.model.Boleta;

import lombok.Data;

// Este DTO representa el formato final de una venta o boleta que será devuelto al Frontend.
// Es decir: esto es lo que React consume cuando consulta una venta registrada.
@Data
public class VentaResponseDTO {

    private Long id;                 // Identificador de la venta/boleta
    private LocalDateTime fecha;     // Momento exacto en que se generó la venta
    private Double total;            // Monto total calculado de la venta
    private Long usuarioId;          // ID del usuario que realizó la compra
    private List<DetalleResponseDTO> detalles; // Lista de productos incluidos en la venta

    @Data
    public static class DetalleResponseDTO {
        private Long id;         // ID del detalle (línea de la boleta)
        private Long productoId; // Producto asociado a este detalle
        private Integer cantidad; // Unidades compradas de ese producto
        private Double subtotal;  // Total correspondiente a esa línea (precio * cantidad)
    }

    // Constructor que transforma automáticamente una entidad Boleta en un DTO listo para enviar al cliente
    public VentaResponseDTO(Boleta boleta) {

        // Copiamos los datos principales directamente desde la entidad JPA
        this.id = boleta.getId();
        this.fecha = boleta.getFecha();
        this.total = boleta.getTotal();
        this.usuarioId = boleta.getUsuarioId();

        // Convertimos cada detalle de la boleta a su correspondiente DTO
        // usando programación funcional (streams) para simplificar el mapeo.
        this.detalles = boleta.getDetalles().stream()
            .map(detalle -> {
                // Por cada detalle se construye su representación DTO
                DetalleResponseDTO dto = new DetalleResponseDTO();
                dto.setId(detalle.getId());
                dto.setProductoId(detalle.getProductoId());
                dto.setCantidad(detalle.getCantidad());
                dto.setSubtotal(detalle.getSubtotal());
                return dto;
            })
            .collect(Collectors.toList()); // Convertimos el stream a una lista final
    }
}
