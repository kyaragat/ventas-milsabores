package com.milsabores.ventas.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Service
public class ProductoClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String PRODUCTO_SERVICE_URL = "http://44.213.57.93:8080/";
    
    // IMPORTANTE: Esta clave debe ser la MISMA que usa el microservicio de usuarios/productos
    private static final String SECRET_KEY = "mySecretKeyForJWTGenerationInMilSaboresApplication2024";
    // Usar la clave completa - DEBE ser igual en todos los microservicios
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private String generateTokenForProduct(Authentication authentication) {
        // Generar un token JWT interno para comunicación entre microservicios
        if (authentication != null) {
            String role = "ADMIN"; 
            
            return Jwts.builder()
                    .setSubject("sistema-interno@milsabores.com") // Email del sistema
                    .claim("userId", 999L) // ID especial para el sistema
                    .claim("role", role)
                    .claim("firstName", "Sistema")
                    .claim("lastName", "Interno")
                    .setIssuedAt(new java.util.Date())
                    .setExpiration(new java.util.Date(System.currentTimeMillis() + 10 * 60 * 1000)) // 10 minutos
                    .signWith(key)
                    .compact();
        }
        return null;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String token = generateTokenForProduct(auth);
            if (token != null) {
                headers.set("Authorization", "Bearer " + token);
                System.out.println("Token JWT generado para llamada a productos: " + token.substring(0, 20) + "...");
            }
        }
        return headers;
    }

    public void reducirStock(Long productoId, Integer cantidad) {
        String url = PRODUCTO_SERVICE_URL + "api/v1/productos/" + productoId + "/reducir-stock?cantidad=" + cantidad;
        System.out.println("=== REDUCIENDO STOCK ===");
        System.out.println("URL: " + url);
        
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
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
        
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<com.milsabores.ventas.dto.ProductoDTO> response = 
                restTemplate.exchange(url, HttpMethod.GET, request, com.milsabores.ventas.dto.ProductoDTO.class);
            com.milsabores.ventas.dto.ProductoDTO producto = response.getBody();
            System.out.println("Producto recibido: " + (producto != null ? producto.getNombre() : "null"));
            return producto;
        } catch (Exception e) {
            System.err.println("Error al obtener producto: " + e.getMessage());
            throw e;
        }
    }
}