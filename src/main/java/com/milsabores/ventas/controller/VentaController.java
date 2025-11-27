package com.milsabores.ventas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.milsabores.ventas.dto.VentaRequestDTO;
import com.milsabores.ventas.dto.VentaResponseDTO;
import com.milsabores.ventas.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ventas") 
@CrossOrigin(origins = "*")
@Tag(name = "Ventas", description = "API para gestión de ventas y órdenes")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping
    @Operation(
        summary = "Crear nueva venta",
        description = "Registra una nueva venta validando stock y calculando total",
        responses = {
            @ApiResponse(responseCode = "201", description = "Venta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente")
        }
    )
    public ResponseEntity<VentaResponseDTO> registrarVenta(
            @Valid @RequestBody VentaRequestDTO ventaRequest,
            Authentication authentication) {
        
        Long usuarioId = (Long) authentication.getPrincipal();
        VentaResponseDTO ventaRegistrada = ventaService.registrarVenta(usuarioId, ventaRequest);
        return new ResponseEntity<>(ventaRegistrada, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Listar todas las ventas",
        description = "Obtiene todas las ventas del sistema (solo para administradores)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
        }
    )
    public ResponseEntity<List<VentaResponseDTO>> listarVentas() {
        List<VentaResponseDTO> ventas = ventaService.listarVentas();
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener venta por ID",
        description = "Obtiene una venta específica por su ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
        }
    )
    public ResponseEntity<VentaResponseDTO> obtenerVentaPorId(
            @Parameter(description = "ID de la venta") 
            @PathVariable Long id,
            Authentication authentication) {
        
        VentaResponseDTO venta = ventaService.obtenerVentaPorId(id);
        
        // Verificar que el usuario sea admin o propietario de la venta
        Long usuarioId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !venta.getId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(venta);
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Obtener ventas de un usuario",
        description = "Obtiene todas las ventas de un usuario específico",
        responses = {
            @ApiResponse(responseCode = "200", description = "Ventas del usuario obtenidas"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
        }
    )
    public ResponseEntity<List<VentaResponseDTO>> listarVentasPorUsuario(
            @Parameter(description = "ID del usuario") 
            @PathVariable Long userId,
            Authentication authentication) {
        
        // Verificar que el usuario sea admin o esté consultando sus propias ventas
        Long usuarioAutenticado = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !userId.equals(usuarioAutenticado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<VentaResponseDTO> ventas = ventaService.listarVentasPorUsuario(userId);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/mis-ventas")
    @Operation(
        summary = "Obtener mis ventas",
        description = "Obtiene todas las ventas del usuario autenticado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Ventas del usuario obtenidas")
        }
    )
    public ResponseEntity<List<VentaResponseDTO>> obtenerMisVentas(Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        List<VentaResponseDTO> ventas = ventaService.listarVentasPorUsuario(usuarioId);
        return ResponseEntity.ok(ventas);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Anular venta",
        description = "Anula una venta y devuelve el stock (solo administradores)",
        responses = {
            @ApiResponse(responseCode = "204", description = "Venta anulada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
        }
    )
    public ResponseEntity<Void> anularBoleta(
            @Parameter(description = "ID de la venta a anular") 
            @PathVariable Long id) {
        
        ventaService.anularVenta(id);
        return ResponseEntity.noContent().build();
    }
}