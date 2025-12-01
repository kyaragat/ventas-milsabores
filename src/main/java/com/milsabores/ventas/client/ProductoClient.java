package com.milsabores.ventas.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductoClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String PRODUCTO_SERVICE_URL = "http://localhost:8080/";

    public void reducirStock(Long productoId, Integer cantidad) {
        String url = PRODUCTO_SERVICE_URL + "api/v1/productos/" + productoId + "/reducir-stock?cantidad=" + cantidad;
        System.out.println("=== REDUCIENDO STOCK ===");
        System.out.println("URL: " + url);
        try {
            restTemplate.put(url, null);
            System.out.println("Stock reducido exitosamente");
        } catch (Exception e) {
            System.err.println("Error al reducir stock: " + e.getMessage());
            throw e;
        }
    }

    public com.milsabores.ventas.dto.ProductoDTO obtenerProductoPorId(Long productoId) {
        String url = PRODUCTO_SERVICE_URL + "api/v1/productos/" + productoId;
        System.out.println("=== LLAMANDO A PRODUCTO CLIENT ===");
        System.out.println("URL: " + url);
        try {
            ResponseEntity<com.milsabores.ventas.dto.ProductoDTO> response = 
                restTemplate.getForEntity(url, com.milsabores.ventas.dto.ProductoDTO.class);
            com.milsabores.ventas.dto.ProductoDTO producto = response.getBody();
            System.out.println("Producto recibido: " + (producto != null ? producto.getNombre() : "null"));
            return producto;
        } catch (Exception e) {
            System.err.println("Error al obtener producto: " + e.getMessage());
            throw e;
        }
    }
}