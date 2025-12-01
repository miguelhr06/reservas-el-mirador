package com.elmirador.reservas.repository;

import com.elmirador.reservas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Para el login y validaciones
    Optional<Usuario> findByEmail(String email);

    // Para evitar registros duplicados
    boolean existsByEmail(String email);

    // Para buscar por documento (DNI)
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
}