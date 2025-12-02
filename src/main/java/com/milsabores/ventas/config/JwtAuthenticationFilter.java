package com.milsabores.ventas.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    // IMPORTANTE: Esta clave debe ser la MISMA que usa el microservicio de usuarios
    private static final String SECRET_KEY = "mySecretKeyForJWTGenerationInMilSaboresApplication2024";
    // Usar la clave completa - DEBE ser igual en todos los microservicios
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Authorization Header: " + authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token extraído: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                
                System.out.println("Claims parseados exitosamente!");
                System.out.println("Subject: " + claims.getSubject());
                System.out.println("User ID: " + claims.get("userId"));
                System.out.println("Role (singular): " + claims.get("role"));
                System.out.println("Roles (plural): " + claims.get("roles"));
                
                String username = claims.getSubject();
                Object userIdObj = claims.get("userId");
                Long userId = null;
                if (userIdObj instanceof Integer) {
                    userId = ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                }
                
                // Buscar roles tanto en formato singular como plural
                List<String> roles = null;
                Object rolesClaim = claims.get("roles");
                Object roleClaim = claims.get("role");
                
                if (rolesClaim instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> rolesList = (List<String>) rolesClaim;
                    roles = rolesList;
                } else if (roleClaim instanceof String) {
                    // Si viene un rol único como string, convertirlo a lista
                    roles = List.of((String) roleClaim);
                }
                
                if (username != null && roles != null) {
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                    
                    System.out.println("Authorities creadas: " + authorities);
                    
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    // Agregamos userId como detalle para poder accederlo desde el controller
                    auth.setDetails(userId);
                    
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("Authentication establecida exitosamente!");
                } else {
                    System.out.println("Username o roles son null - no se establece authentication");
                }
                
            } catch (Exception e) {
                System.out.println("ERROR al procesar JWT: " + e.getMessage());
                e.printStackTrace();
                logger.error("No se pudo extraer información del JWT: " + e.getMessage());
            }
        } else {
            System.out.println("No se encontró token Bearer en el header");
        }
        
        System.out.println("Authentication final: " + SecurityContextHolder.getContext().getAuthentication());
        System.out.println("========================");
        
        
        filterChain.doFilter(request, response);
    }
}