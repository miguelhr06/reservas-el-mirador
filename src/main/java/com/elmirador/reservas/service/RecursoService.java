package com.elmirador.reservas.service;

import com.elmirador.reservas.model.Recurso;
import com.elmirador.reservas.repository.RecursoRepository;
import com.elmirador.reservas.repository.TipoRecursoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecursoService {

    private final RecursoRepository recursoRepository;
    private final TipoRecursoRepository tipoRecursoRepository;

    /**
     * Busca qué recursos están libres en un rango de fechas.
     * Valida que las fechas sean lógicas.
     */
    public List<Recurso> buscarDisponibles(LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Validar que fecha INICIO no sea anterior a HOY (con un margen de 1 minuto
        // por latencia)
        if (inicio.isBefore(ahora.minusMinutes(1))) {
            throw new IllegalArgumentException("La fecha de inicio no puede estar en el pasado.");
        }

        // 2. Validar que fecha FIN sea posterior a fecha INICIO
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la fecha de llegada.");
        }

        // 3. Validar duración MÍNIMA de 24 horas (1 día)
        // Usamos HOURS para ser precisos con las 24h
        long horasEstadia = ChronoUnit.HOURS.between(inicio, fin);
        if (horasEstadia < 24) {
            throw new IllegalArgumentException("La estancia mínima es de 24 horas (1 día).");
        }

        // 4. Validar duración MÁXIMA de 30 días
        // Usamos DAYS.between, que cuenta días completos
        long diasEstadia = ChronoUnit.DAYS.between(inicio, fin);
        // Ojo: si reservas del 1 al 31, son 30 días exactos. Si pasa de 30, error.
        if (diasEstadia > 30) {
            throw new IllegalArgumentException("No se permiten reservas mayores a 30 días.");
        }

        // 5. Validar anticipación MÁXIMA (6 meses a futuro)
        // Regla: La fecha de INICIO no puede ser mayor a HOY + 6 MESES
        LocalDateTime fechaLimiteFutura = ahora.plusMonths(6);
        if (inicio.isAfter(fechaLimiteFutura)) {
            throw new IllegalArgumentException("Solo aceptamos reservas con un máximo de 6 meses de anticipación.");
        }

        // Si pasa todas las validaciones, consultamos a la BD
        return recursoRepository.findRecursosDisponibles(inicio, fin);
    }

    public Recurso obtenerPorId(Integer id) {
        return recursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado"));
    }

    // --- MÉTODOS PARA EL CRUD ---

    public List<Recurso> listarTodos() {
        return recursoRepository.findAll();
    }

    public List<com.elmirador.reservas.model.TipoRecurso> listarTipos() {
        return tipoRecursoRepository.findAll();
    }

    public void guardar(Recurso recurso) {
        recursoRepository.save(recurso);
    }

    public void eliminar(Integer id) {
        recursoRepository.deleteById(id);
    }
}