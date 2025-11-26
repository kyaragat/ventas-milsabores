package com.milsabores.ventas.service;

import java.util.List;

import com.milsabores.ventas.dto.VentaRequestDTO;
import com.milsabores.ventas.dto.VentaResponseDTO;

// Interfaz que define las operaciones principales del servicio de ventas
public interface VentaService {

    // Registra una nueva venta/boleta
    VentaResponseDTO registrarVenta(VentaRequestDTO ventaRequest);

    // Obtiene todas las ventas registradas
    List<VentaResponseDTO> listarVentas();

    // Busca una venta específica por su ID
    VentaResponseDTO obtenerVentaPorId(Long id);

    // Lista todas las ventas realizadas por un usuario
    List<VentaResponseDTO> listarVentasPorUsuario(Long userId);

    // Elimina o anula una venta
    void anularVenta(Long id);
}
