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
        
        System.out.println("=== CONTROLLER registrarVenta DEBUG ===");
        System.out.println("Authentication object: " + authentication);
        System.out.println("Authentication name: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("Authentication authorities: " + (authentication != null ? authentication.getAuthorities() : "null"));
        System.out.println("Authentication details: " + (authentication != null ? authentication.getDetails() : "null"));
        System.out.println("VentaRequest: " + ventaRequest);
        
        // Extraer userId del JWT (requerido para usuarios autenticados)
        Long usuarioId = null;

        if (authentication != null) {
            if (authentication.getDetails() != null) {
                try {
                    usuarioId = Long.parseLong(authentication.getDetails().toString());
                    System.out.println("Usuario ID extraído de details: " + usuarioId);
                } catch (NumberFormatException e) {
                    System.err.println("No se pudo parsear userId desde details: " + authentication.getDetails());
                }
            }

            // Si no se pudo obtener desde details, usar email
            if (usuarioId == null && authentication.getName() != null) {
                String email = authentication.getName();
                System.out.println("Buscando userId por email: " + email);
                usuarioId = ventaService.obtenerUserIdPorEmail(email);
                if (usuarioId != null) {
                    System.out.println("Usuario ID encontrado por email: " + usuarioId);
                }
            }
        } else {
            System.err.println("Authentication es NULL!");
        }

        // Si aún no se pudo obtener, error
        if (usuarioId == null) {
            System.err.println("No se pudo identificar al usuario para la venta");
            System.out.println("====================================");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println("Procediendo a registrar venta con usuarioId: " + usuarioId);
        VentaResponseDTO ventaRegistrada = ventaService.registrarVenta(usuarioId, ventaRequest);
        System.out.println("Venta registrada exitosamente: " + ventaRegistrada);
        System.out.println("====================================");
        return new ResponseEntity<>(ventaRegistrada, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @Operation(
        summary = "Listar todas las ventas",
        description = "Obtiene todas las ventas del sistema (para administradores y vendedores)",
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
        
        // ADMIN y VENDEDOR pueden ver cualquier venta
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_VENDEDOR"));

        if (!isAdmin) {
            // CLIENTE solo puede ver sus propias ventas
            Long currentUserId = null;
            try {
                if (authentication.getDetails() != null) {
                    currentUserId = Long.parseLong(authentication.getDetails().toString());
                } else {
                    String email = authentication.getName();
                    currentUserId = ventaService.obtenerUserIdPorEmail(email);
                }
                if (currentUserId != null && !currentUserId.equals(venta.getUsuarioId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
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
        
        // Verificar que el usuario solo pueda ver sus propias ventas (excepto ADMIN)
        try {
            Long currentUserId;

            // Intentar obtener userId desde los detalles
            if (authentication.getDetails() != null && !authentication.getDetails().toString().equals("null")) {
                currentUserId = Long.parseLong(authentication.getDetails().toString());
            } else {
                // El JWT no incluye userId, pero tenemos el email como subject
                String email = authentication.getName();
                System.out.println("Obteniendo userId para email: " + email);

                currentUserId = ventaService.obtenerUserIdPorEmail(email);
                if (currentUserId == null) {
                    System.err.println("No se encontró usuario con email: " + email);
                    return ResponseEntity.badRequest().build();
                }
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (NumberFormatException e) {
            System.err.println("Error al convertir userId a Long: " + e.getMessage());
            return ResponseEntity.badRequest().build();
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
        try {
            // Obtener userId directamente del JWT
            Long usuarioId = null;
            if (authentication.getDetails() != null && !authentication.getDetails().toString().equals("null")) {
                try {
                    usuarioId = (Long) authentication.getDetails();
                } catch (ClassCastException e) {
                    usuarioId = Long.parseLong(authentication.getDetails().toString());
                }
            }

            if (usuarioId == null) {
                System.err.println("No se pudo obtener userId del JWT token para mis-ventas");
                return ResponseEntity.badRequest().build();
            }

            List<VentaResponseDTO> ventas = ventaService.listarVentasPorUsuario(usuarioId);
            return ResponseEntity.ok(ventas);
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear userId: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/mis-compras")
    @Operation(
        summary = "Obtener mis compras",
        description = "Obtiene el historial de compras del usuario autenticado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Historial de compras obtenido")
        }
    )
    public ResponseEntity<List<VentaResponseDTO>> obtenerMisCompras(Authentication authentication) {
        try {
            System.out.println("=== DEBUG LISTAR MIS COMPRAS ===");
            System.out.println("Authentication name: " + authentication.getName());
            System.out.println("Authentication details: " + authentication.getDetails());
            System.out.println("Authentication authorities: " + authentication.getAuthorities());

            // Obtener userId directamente del JWT
            Long userId = null;
            if (authentication.getDetails() != null && !authentication.getDetails().toString().equals("null")) {
                try {
                    userId = (Long) authentication.getDetails();
                    System.out.println("UserId desde JWT details: " + userId);
                } catch (ClassCastException e) {
                    // Intentar parsear como String si no es Long
                    userId = Long.parseLong(authentication.getDetails().toString());
                    System.out.println("UserId parseado desde details: " + userId);
                }
            }

            if (userId == null) {
                System.err.println("No se pudo obtener userId del JWT token");
                return ResponseEntity.badRequest().build();
            }

            System.out.println("UserId final para buscar: " + userId);

            List<VentaResponseDTO> ventas = ventaService.listarVentasPorUsuario(userId);
            System.out.println("Número de ventas encontradas: " + ventas.size());

            return ResponseEntity.ok(ventas);

        } catch (NumberFormatException e) {
            System.err.println("Error al convertir userId a Long: " + e.getMessage() + ". Authentication name: " + authentication.getName() + ", details: " + authentication.getDetails());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error al obtener las compras del usuario: " + e.getMessage() + ". Authentication: " + authentication);
            return ResponseEntity.badRequest().build();
        }
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