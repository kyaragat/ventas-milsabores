package com.milsabores.ventas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.milsabores.ventas.dto.ProductoDTO;
import com.milsabores.ventas.exception.ResourceNotFoundException;

import java.math.BigDecimal;

@Service
public class ProductoClientService {
    
    @Value("${productos.service.url:http://localhost:8080}")
    private String productosServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public ProductoClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public ProductoDTO obtenerProductoPorId(Long id) {
        try {
            String url = productosServiceUrl + "/api/productos/" + id;
            return restTemplate.getForObject(url, ProductoDTO.class);
        } catch (Exception e) {
            // Para desarrollo, simulamos datos
            return simularProducto(id);
        }
    }
    
    public void actualizarStock(Long id, Integer cantidad) {
        // Como el servicio de productos no tiene endpoint para actualizar stock,
        // simplemente registramos la operación. El stock se maneja en la tabla inventario
        // que es independiente del servicio de ventas.
        System.out.println("Actualización de stock registrada - Producto: " + id + ", Cantidad: " + cantidad);
    }
    
    private ProductoDTO simularProducto(Long id) {
        // Datos simulados para desarrollo
        ProductoDTO producto = new ProductoDTO();
        producto.setId(id);
        producto.setNombre("Producto " + id);
        producto.setDescripcion("Descripción del producto " + id);
        producto.setPrecio(BigDecimal.valueOf(1000 + (id * 100)));
        producto.setStock(10);
        producto.setImagen("/img/producto" + id + ".jpg");
        producto.setCategoriaId(1L);
        producto.setCategoriaNombre("Categoría Test");
        return producto;
    }
}