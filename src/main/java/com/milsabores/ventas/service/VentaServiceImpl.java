package com.milsabores.ventas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.milsabores.ventas.dto.VentaRequestDTO;
import com.milsabores.ventas.dto.VentaResponseDTO;
import com.milsabores.ventas.exception.ResourceNotFoundException;
import com.milsabores.ventas.model.Boleta;
import com.milsabores.ventas.model.DetalleBoleta;
import com.milsabores.ventas.repository.BoletaRepository;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private BoletaRepository boletaRepository;

    // En una implementación real, aquí también se integraría un cliente Feign/RestTemplate
    // para comunicarse con el microservicio de Productos.

    @Override
    @Transactional // Garantiza que se ejecute todo como una única operación
    public VentaResponseDTO registrarVenta(VentaRequestDTO ventaRequest) {
        
        Boleta boleta = new Boleta();
        boleta.setUsuarioId(ventaRequest.getUsuarioId());
        
        double totalVenta = 0.0;
        List<DetalleBoleta> detalles = new ArrayList<>();

        for (VentaRequestDTO.DetalleRequestDTO detalleRequest : ventaRequest.getDetalles()) {
            
            // --- PROCESO DE NEGOCIO ---
            // 1. Consultar precio y validar stock desde el microservicio de Productos.
            //    Ejemplo:
            //    ProductoDTO producto = productoCliente.getProducto(detalleRequest.getProductoId());
            //    if(producto.getStock() < detalleRequest.getCantidad()) { ... }

            // Precio temporal usado solo para pruebas
            double precioProducto = 50.0;  // Reemplazar cuando exista el microservicio
            
            // 2. Cálculo del subtotal
            double subtotal = precioProducto * detalleRequest.getCantidad();
            
            // 3. Crear el detalle de la venta
            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setProductoId(detalleRequest.getProductoId());
            detalle.setCantidad(detalleRequest.getCantidad());
            detalle.setSubtotal(subtotal);
            detalle.setBoleta(boleta); // Conecta detalle con la boleta principal
            
            detalles.add(detalle);
            totalVenta += subtotal;

            // 4. Descontar stock (cuando se conecte el servicio de Productos)
            //    productoCliente.reducirStock(...);
        }
        
        boleta.setTotal(totalVenta);
        boleta.setDetalles(detalles);
        
        // Al guardar la boleta, también se almacenan sus detalles por el CascadeType.ALL
        Boleta boletaGuardada = boletaRepository.save(boleta);
        
        return new VentaResponseDTO(boletaGuardada);
    }

    @Override
    public List<VentaResponseDTO> listarVentas() {
        return boletaRepository.findAll().stream()
                .map(VentaResponseDTO::new)  // Convierte Boleta → DTO
                .collect(Collectors.toList());
    }

    @Override
    public VentaResponseDTO obtenerVentaPorId(Long id) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta no encontrada con id: " + id));
        return new VentaResponseDTO(boleta);
    }

    @Override
    public List<VentaResponseDTO> listarVentasPorUsuario(Long userId) {
        return boletaRepository.findByUsuarioId(userId).stream()
                .map(VentaResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void anularVenta(Long id) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta no encontrada con id: " + id));

        // Aquí iría la lógica para devolver stock al microservicio de Productos.

        boletaRepository.delete(boleta);
    }
}
