package com.elmirador.reservas.repository;

import com.elmirador.reservas.model.Recurso;
import com.elmirador.reservas.model.enums.EstadoRecurso;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Integer> {

        // Buscar por tipo (ej: Dame todos los BUNGALOWS)
        List<Recurso> findByTipoNombreAndEstado(String nombreTipo, EstadoRecurso estado);

        /**
         * QUERY MAESTRA DE DISPONIBILIDAD
         * Selecciona los recursos que:
         * 1. Estén en estado DISPONIBLE.
         * 2. NO estén en la lista de recursos reservados (ocupados) para esas fechas.
         * * Lógica de colisión de fechas: (ReservaInicio < UserFin) Y (ReservaFin >
         * UserInicio)
         */
        @Query("SELECT r FROM Recurso r " +
                        "WHERE r.estado = com.elmirador.reservas.model.enums.EstadoRecurso.DISPONIBLE " +
                        "AND r.idRecurso NOT IN (" +
                        "SELECT res.recurso.idRecurso FROM Reserva res " +
                        "WHERE res.estado IN (" +
                        "com.elmirador.reservas.model.enums.EstadoReserva.CONFIRMADA, " +
                        "com.elmirador.reservas.model.enums.EstadoReserva.PENDIENTE_PAGO, " + // Ya ocultaba estos
                        "com.elmirador.reservas.model.enums.EstadoReserva.ESPERANDO_CONFIRMACION" + // AGREGAR ESTE
                        ") " +
                        "AND ((res.fechaInicio < :fechaFin) AND (res.fechaFin > :fechaInicio))" +
                        ")")
        List<Recurso> findRecursosDisponibles(@Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        // Cambia OPTIMISTIC_FORCE_INCREMENT por PESSIMISTIC_WRITE
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT r FROM Recurso r WHERE r.idRecurso = :id")
        Optional<Recurso> findByIdWithLock(@Param("id") Integer id);
}