package com.milsabores.ventas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boletas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "usuario_id")
    private Long usuarioId;
    
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(name = "nombre_cliente")
    private String nombreCliente;
    
    @Column(name = "email_cliente")
    private String emailCliente;
    
    @Column(name = "telefono_cliente")
    private String telefonoCliente;
    
    @NotBlank(message = "La dirección es obligatoria")
    @Column(name = "direccion_envio")
    private String direccionEnvio;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<DetalleBoleta> detalles;
    
    public enum EstadoVenta {
        PENDIENTE,
        CONFIRMADA,
        ENVIADA,
        ENTREGADA,
        CANCELADA
    }
}
