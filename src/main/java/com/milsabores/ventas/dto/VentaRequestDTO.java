package com.milsabores.ventas.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class VentaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;
    
    @Email(message = "Email inválido")
    private String emailCliente;
    
    private String telefonoCliente;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String direccionEnvio;

    @NotNull(message = "La lista de detalles no puede ser nula")
    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<DetalleRequestDTO> detalles;

    @Data
    public static class DetalleRequestDTO {

        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidad;
    }
}
