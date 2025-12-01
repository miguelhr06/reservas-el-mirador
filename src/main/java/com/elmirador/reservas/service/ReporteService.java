package com.elmirador.reservas.service;

import com.elmirador.reservas.model.Reserva;
import com.elmirador.reservas.model.enums.EstadoReserva;
import com.elmirador.reservas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReservaRepository reservaRepository;

    // 1. Calcular Ingresos Totales (Solo de reservas CONFIRMADAS o COMPLETADAS)
    public BigDecimal calcularIngresosTotales() {
        return reservaRepository.findAll().stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.COMPLETADA)
                .map(Reserva::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 2. Contar reservas por estado (Para gráfico de pastel)
    public Map<String, Long> obtenerEstadisticasPorEstado() {
        List<Reserva> todas = reservaRepository.findAll();
        return todas.stream()
                .collect(Collectors.groupingBy(r -> r.getEstado().name(), Collectors.counting()));
    }

    // 3. Obtener ocupación del mes actual (Reservas por día)
    public Map<Integer, Long> obtenerOcupacionMesActual() {
        LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime finMes = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);

        List<Reserva> reservasMes = reservaRepository.findByFechaInicioBetween(inicioMes, finMes);

        // Agrupar por día del mes (1, 2, ... 30)
        return reservasMes.stream()
                .collect(Collectors.groupingBy(r -> r.getFechaInicio().getDayOfMonth(), Collectors.counting()));
    }

    public long contarReservasTotales() {
        return reservaRepository.count();
    }
}