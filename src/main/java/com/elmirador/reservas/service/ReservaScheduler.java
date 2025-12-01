package com.elmirador.reservas.service;

import com.elmirador.reservas.model.Reserva;
import com.elmirador.reservas.model.enums.EstadoReserva;
import com.elmirador.reservas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaScheduler {

    private final ReservaRepository reservaRepository;

    // Se ejecuta cada 1 hora (3600000 ms)
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cancelarReservasExpiradas() {
        // Lógica: Buscar reservas PENDIENTE_PAGO creadas hace más de 24 horas
        LocalDateTime limite = LocalDateTime.now().minusHours(24);

        // Necesitas agregar este método en tu ReservaRepository (ver abajo)
        List<Reserva> reservasVencidas = reservaRepository.findByEstadoAndCreatedAtBefore(EstadoReserva.PENDIENTE_PAGO,
                limite);

        for (Reserva r : reservasVencidas) {
            r.setEstado(EstadoReserva.CANCELADA);
            // Opcional: Liberar el recurso es implícito al cambiar estado a CANCELADA
            System.out.println("Reserva expirada cancelada automáticamente: " + r.getCodigoReserva());
        }
        reservaRepository.saveAll(reservasVencidas);
    }
}