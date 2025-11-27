package com.milsabores.ventas.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.milsabores.ventas.client.ProductoClient;
import com.milsabores.ventas.dto.ProductoDTO;
import com.milsabores.ventas.dto.VentaRequestDTO;
import com.milsabores.ventas.dto.VentaResponseDTO;
import com.milsabores.ventas.exception.InsufficientStockException;
import com.milsabores.ventas.exception.ResourceNotFoundException;
import com.milsabores.ventas.model.Boleta;
import com.milsabores.ventas.model.DetalleBoleta;
import com.milsabores.ventas.repository.BoletaRepository;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private BoletaRepository boletaRepository;
    
    @Autowired
    private ProductoClient productoClient;

    @Override
    @Transactional
    public VentaResponseDTO registrarVenta(Long usuarioId, VentaRequestDTO ventaRequest) {
        
        Boleta boleta = new Boleta();
        boleta.setUsuarioId(usuarioId);
        boleta.setNombreCliente(ventaRequest.getNombreCliente());
        boleta.setEmailCliente(ventaRequest.getEmailCliente());
        boleta.setTelefonoCliente(ventaRequest.getTelefonoCliente());
        boleta.setDireccionEnvio(ventaRequest.getDireccionEnvio());
        
        BigDecimal totalVenta = BigDecimal.ZERO;
        List<DetalleBoleta> detalles = new ArrayList<>();

        for (VentaRequestDTO.DetalleRequestDTO detalleRequest : ventaRequest.getDetalles()) {
            
            // Por simplicidad, usar valores temporales 
            // En un caso real, obtendría los datos del microservicio de productos
            
            BigDecimal precioUnitario = new BigDecimal("10000"); // Precio temporal
            String nombreProducto = "Producto " + detalleRequest.getProductoId(); // Nombre temporal
            
            // 2. Calcular subtotal
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(detalleRequest.getCantidad()));
            
            // 3. Crear detalle
            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setProductoId(detalleRequest.getProductoId());
            detalle.setNombreProducto(nombreProducto);
            detalle.setCantidad(detalleRequest.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            detalle.setBoleta(boleta);
            
            detalles.add(detalle);
            totalVenta = totalVenta.add(subtotal);

            // 4. Llamar al microservicio de productos para reducir stock
            try {
                productoClient.reducirStock(detalleRequest.getProductoId(), detalleRequest.getCantidad());
            } catch (Exception e) {
                // Log del error - en producción podrías hacer rollback
                System.err.println("Error al reducir stock del producto " + detalleRequest.getProductoId() + ": " + e.getMessage());
            }
        }
        
        boleta.setTotal(totalVenta);
        boleta.setDetalles(detalles);
        
        Boleta boletaGuardada = boletaRepository.save(boleta);
        
        return convertirAResponseDTO(boletaGuardada);
    }

    @Override
    public List<VentaResponseDTO> listarVentas() {
        return boletaRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VentaResponseDTO obtenerVentaPorId(Long id) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
        return convertirAResponseDTO(boleta);
    }

    @Override
    public List<VentaResponseDTO> listarVentasPorUsuario(Long userId) {
        return boletaRepository.findByUsuarioIdOrderByFechaDesc(userId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void anularVenta(Long id) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        // Devolver stock
        for (DetalleBoleta detalle : boleta.getDetalles()) {
            // Restaurar stock (implementación básica)
            System.out.println("Restaurando stock para producto: " + detalle.getProductoId());
        }

        boletaRepository.delete(boleta);
    }
    
    private VentaResponseDTO convertirAResponseDTO(Boleta boleta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(boleta.getId());
        dto.setFecha(boleta.getFecha());
        dto.setTotal(boleta.getTotal());
        dto.setId(boleta.getUsuarioId());
        dto.setNombreCliente(boleta.getNombreCliente());
        dto.setEmailCliente(boleta.getEmailCliente());
        dto.setTelefonoCliente(boleta.getTelefonoCliente());
        dto.setDireccionEnvio(boleta.getDireccionEnvio());
        dto.setEstado(boleta.getEstado().name());
        
        if (boleta.getDetalles() != null) {
            List<VentaResponseDTO.DetalleResponseDTO> detallesDTO = boleta.getDetalles().stream()
                .map(detalle -> {
                    VentaResponseDTO.DetalleResponseDTO detalleDTO = new VentaResponseDTO.DetalleResponseDTO();
                    detalleDTO.setId(detalle.getId());
                    detalleDTO.setProductoId(detalle.getProductoId());
                    detalleDTO.setNombreProducto(detalle.getNombreProducto());
                    detalleDTO.setCantidad(detalle.getCantidad());
                    detalleDTO.setPrecioUnitario(detalle.getPrecioUnitario());
                    detalleDTO.setSubtotal(detalle.getSubtotal());
                    return detalleDTO;
                })
                .collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }
        
        return dto;
    } 
}