package com.milsabores.ventas.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.milsabores.ventas.dto.ProductoDTO;

// @FeignClient(name = "productos-service", url = "${productos.service.url}")
public interface ProductoClient {
    
    @GetMapping("/api/v1/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable Long id);
    
    @PutMapping("/api/v1/productos/{id}/stock")
    void actualizarStock(@PathVariable Long id, @RequestParam Integer cantidad);
}