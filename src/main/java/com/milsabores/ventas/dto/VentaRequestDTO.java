package com.milsabores.ventas.dto;

import java.util.List;

import lombok.Data;

// Este DTO representa la información que llega desde el cliente al momento de registrar una venta.
// En otras palabras, es el contenido que se envía en el cuerpo (body) del POST desde el Frontend.
@Data
public class VentaRequestDTO {

    // ID del usuario que realiza la compra
    private Long usuarioId;

    // Lista de los productos incluidos en la venta, cada uno con su cantidad
    private List<DetalleRequestDTO> detalles;

    @Data
    public static class DetalleRequestDTO {

        // Identificador único del producto comprado
        private Long productoId;

        // Cantidad que el usuario desea comprar de ese producto
        private Integer cantidad;

        // Importante: el precio unitario y el subtotal NO se aceptan desde el Frontend.
        // Esto se determina en el servidor para evitar que se manipulen los valores.
        // Esta lógica evita fraudes, errores o modificaciones del lado del usuario.
    }
}
