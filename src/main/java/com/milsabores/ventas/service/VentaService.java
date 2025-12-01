package com.milsabores.ventas.service;

import java.util.List;

import com.milsabores.ventas.dto.VentaRequestDTO;
import com.milsabores.ventas.dto.VentaResponseDTO;

public interface VentaService {

    VentaResponseDTO registrarVenta(Long usuarioId, VentaRequestDTO ventaRequest);

    List<VentaResponseDTO> listarVentas();

    VentaResponseDTO obtenerVentaPorId(Long id);

    List<VentaResponseDTO> listarVentasPorUsuario(Long userId);

    void anularVenta(Long id);

    Long obtenerUserIdPorEmail(String email);
}
