package com.milsabores.ventas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.milsabores.ventas.dto.ProductoDTO;
import com.milsabores.ventas.exception.ResourceNotFoundException;

import java.math.BigDecimal;

@Service
public class ProductoClientService {
    
    @Value("${productos.service.url}")
    private String productosServiceUrl;
    
    private RestTemplate restTemplate = new RestTemplate();
    
    public ProductoDTO obtenerProductoPorId(Long id) {
        try {
            String url = productosServiceUrl + "/api/v1/productos/" + id;
            return restTemplate.getForObject(url, ProductoDTO.class);
        } catch (Exception e) {
            // Para desarrollo, simulamos datos
            return simularProducto(id);
        }
    }
    
    public void actualizarStock(Long id, Integer cantidad) {
        try {
            String url = productosServiceUrl + "/api/v1/productos/" + id + "/stock?cantidad=" + cantidad;
            restTemplate.put(url, null);
        } catch (Exception e) {
            // En desarrollo, solo logueamos
            System.out.println("Simulando actualización de stock para producto " + id + " cantidad: " + cantidad);
        }
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