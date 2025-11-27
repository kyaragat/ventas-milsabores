package com.milsabores.ventas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.milsabores.ventas.model.Boleta;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponseDTO {

    private Long id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;
    private String direccionEnvio;
    private String estado;
    private List<DetalleResponseDTO> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleResponseDTO {
        private Long id;
        private Long productoId;
        private String nombreProducto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }

    // Constructor para compatibilidad con modelo actual
    public VentaResponseDTO(Boleta boleta) {
        this.id = boleta.getId();
        this.fecha = boleta.getFecha();
        this.total = boleta.getTotal();
        this.nombreCliente = boleta.getNombreCliente();
        this.emailCliente = boleta.getEmailCliente();
        this.telefonoCliente = boleta.getTelefonoCliente();
        this.direccionEnvio = boleta.getDireccionEnvio();
        this.estado = boleta.getEstado() != null ? boleta.getEstado().name() : "PENDIENTE";
        
        this.detalles = boleta.getDetalles().stream()
            .map(detalle -> {
                DetalleResponseDTO dto = new DetalleResponseDTO();
                dto.setId(detalle.getId());
                dto.setProductoId(detalle.getProductoId());
                dto.setNombreProducto(detalle.getNombreProducto());
                dto.setCantidad(detalle.getCantidad());
                dto.setPrecioUnitario(detalle.getPrecioUnitario());
                dto.setSubtotal(detalle.getSubtotal());
                return dto;
            })
            .collect(Collectors.toList());
    }
}
