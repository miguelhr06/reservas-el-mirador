package com.elmirador.reservas.service;

import com.elmirador.reservas.model.Recurso;
import com.elmirador.reservas.model.Reserva;
import com.elmirador.reservas.model.Usuario;
import com.elmirador.reservas.model.enums.EstadoReserva;
import com.elmirador.reservas.repository.RecursoRepository;
import com.elmirador.reservas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final RecursoService recursoService;
    private final UsuarioService usuarioService;
    private final RecursoRepository recursoRepository;

    @Transactional
    public Reserva crearReserva(String emailUsuario, Integer idRecurso, LocalDateTime inicio, LocalDateTime fin,
            Integer cantidadPersonas) {

        // 1. Obtener usuario
        Usuario usuario = usuarioService.buscarPorEmail(emailUsuario);

        // 2. Obtener recurso CON BLOQUEO (Cambio Importante Aquí)
        // Esto asegura que si dos hilos entran aquí, el segundo fallará al intentar
        // guardar al final.
        Recurso recurso = recursoRepository.findByIdWithLock(idRecurso)
                .orElseThrow(() -> new RuntimeException("Recurso no encontrado"));

        // 3. Validar aforo
        if (cantidadPersonas > recurso.getCapacidad()) {
            throw new RuntimeException("La cantidad de personas excede la capacidad.");
        }

        // 4. Re-validar disponibilidad
        // Es vital volver a chequear aquí dentro de la transacción bloqueada
        boolean estaOcupado = recursoRepository.findRecursosDisponibles(inicio, fin)
                .stream().noneMatch(r -> r.getIdRecurso().equals(idRecurso));

        if (estaOcupado) {
            // Aquí caerá el Usuario B
            throw new RuntimeException("Lo sentimos, este recurso ya no está disponible.");
        }

        // 5. Calcular Precio y Generar Código (Igual que antes)
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin);
        if (dias < 1)
            dias = 1;
        java.math.BigDecimal precioTotal = recurso.getPrecioBase().multiply(new java.math.BigDecimal(dias));
        String codigo = "RES-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 6. Guardar
        Reserva nuevaReserva = Reserva.builder()
                .codigoReserva(codigo)
                .usuario(usuario)
                .recurso(recurso)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .cantidadPersonas(cantidadPersonas)
                .precioTotal(precioTotal)
                .estado(com.elmirador.reservas.model.enums.EstadoReserva.PENDIENTE_PAGO)
                .origen("WEB")
                .build();

        return reservaRepository.save(nuevaReserva);

        // Al terminar este método (@Transactional), Spring intentará actualizar la
        // versión del Recurso.
        // Si alguien más lo modificó mientras corríamos este código, lanzará
        // ObjectOptimisticLockingFailureException
    }

    // Agrega esto en ReservaService.java

    public java.util.List<Reserva> listarReservasPorUsuario(String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return reservaRepository.findByUsuarioIdUsuarioOrderByFechaInicioDesc(usuario.getIdUsuario());
    }

    @Transactional
    public void cancelarReservaUsuario(Integer idReserva, String emailUsuario) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Seguridad: Verificar que la reserva sea del usuario logueado
        if (!reserva.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para cancelar esta reserva.");
        }

        // Regla de Negocio: Solo PENDIENTE_PAGO se puede cancelar por el usuario
        if (reserva.getEstado() == EstadoReserva.PENDIENTE_PAGO) {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reservaRepository.save(reserva);
        } else {
            throw new RuntimeException("No se puede cancelar una reserva que ya está pagada o en proceso.");
        }
    }

    // Método para buscar una reserva por su ID
    public Reserva buscarPorId(Integer id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con el ID: " + id));
    }
}