package com.milsabores.ventas.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.milsabores.ventas.model.Boleta;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findByUsuarioId(Long usuarioId);
    
    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    
    @Query("SELECT b FROM Boleta b WHERE b.fecha BETWEEN :inicio AND :fin")
    List<Boleta> findByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    @Query("SELECT b FROM Boleta b WHERE b.estado = :estado")
    List<Boleta> findByEstado(@Param("estado") Boleta.EstadoVenta estado);
}
