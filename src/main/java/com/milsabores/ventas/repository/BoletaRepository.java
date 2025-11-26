package com.milsabores.ventas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.milsabores.ventas.model.Boleta;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    // Busca todas las boletas asociadas a un usuario específico
    List<Boleta> findByUsuarioId(Long usuarioId);
}
