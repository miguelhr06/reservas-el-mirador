package com.elmirador.reservas.repository;

import com.elmirador.reservas.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Método mágico: Spring crea el SELECT * FROM roles WHERE nombre = ?
    Optional<Rol> findByNombre(String nombre);
}