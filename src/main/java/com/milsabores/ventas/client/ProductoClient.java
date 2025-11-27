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

    public ResponseEntity<String> reducirStock(Long productoId, Integer cantidad) {
        String url = PRODUCTO_SERVICE_URL + "/api/productos/" + productoId + "/reducir-stock/" + cantidad;
        return restTemplate.postForEntity(url, null, String.class);
    }
}