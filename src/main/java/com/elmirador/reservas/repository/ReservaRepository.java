package com.elmirador.reservas.repository;

import com.elmirador.reservas.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    // Buscar reserva por su código único (ej: "RES-90210")
    Optional<Reserva> findByCodigoReserva(String codigoReserva);

    // Ver historial de un cliente específico
    List<Reserva> findByUsuarioIdUsuarioOrderByFechaInicioDesc(Integer idUsuario);

    // Para reportes: Buscar reservas entre fechas
    // Requisito: Generación de reportes de ocupación [cite: 216]
    List<Reserva> findByFechaInicioBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    boolean existsByCodigoReserva(String codigoReserva);

    List<Reserva> findByEstadoAndCreatedAtBefore(com.elmirador.reservas.model.enums.EstadoReserva estado,
            java.time.LocalDateTime fecha);
}