package com.milsabores.ventas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/ventas") 
@CrossOrigin(origins = "*") // Habilita CORS para aceptar solicitudes de cualquier origen
public class VentaController {

    @Autowired
    private VentaService ventaService;

    // POST /api/v1/sales → permite crear una nueva venta
    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrarVenta(@RequestBody VentaRequestDTO ventaRequest) {
        VentaResponseDTO ventaRegistrada = ventaService.registrarVenta(ventaRequest);
        return new ResponseEntity<>(ventaRegistrada, HttpStatus.CREATED);
    }

    // GET /api/v1/sales → devuelve el listado completo de ventas
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarVentas() {
        // Nota: este endpoint debería tener restricción (solo Admin)
        List<VentaResponseDTO> ventas = ventaService.listarVentas();
        return ResponseEntity.ok(ventas);
    }

    // GET /api/v1/sales/{id} → obtiene una venta específica según su ID
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtenerVentaPorId(@PathVariable Long id) {
        VentaResponseDTO venta = ventaService.obtenerVentaPorId(id);
        return ResponseEntity.ok(venta);
    }

    // GET /api/v1/sales/user/{userId} → muestra todas las ventas realizadas por un usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VentaResponseDTO>> listarVentasPorUsuario(@PathVariable Long userId) {
        List<VentaResponseDTO> ventas = ventaService.listarVentasPorUsuario(userId);
        return ResponseEntity.ok(ventas);
    }

    // DELETE /api/v1/sales/{id} → elimina o anula una venta por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anularBoleta(@PathVariable Long id) {
        // Nota: este endpoint también debería ser exclusivo para Admin
        ventaService.anularVenta(id);
        return ResponseEntity.noContent().build(); // Retorna 204 cuando no hay contenido en la respuesta
    }
}