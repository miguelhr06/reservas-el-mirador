package com.elmirador.reservas.controller;

import com.elmirador.reservas.model.Reserva;
import com.elmirador.reservas.repository.ReservaRepository;
import com.elmirador.reservas.service.PagoService;
import com.elmirador.reservas.service.ReporteService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/recepcion")
@RequiredArgsConstructor
public class GestionController {

    private final ReservaRepository reservaRepository; // Para listar todas
    private final PagoService pagoService;
    private final ReporteService reporteService;

    // 1. Dashboard principal: Listado de todas las reservas
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Listado de Reservas (Ya lo tenías)
        List<Reserva> todasLasReservas = reservaRepository.findAll();
        model.addAttribute("reservas", todasLasReservas);

        // 2. Datos para Tarjetas (KPIs)
        model.addAttribute("totalIngresos", reporteService.calcularIngresosTotales());
        model.addAttribute("cantidadReservas", reporteService.contarReservasTotales());

        // 3. Datos para Gráficos (Lo pasamos al modelo)
        model.addAttribute("statsEstado", reporteService.obtenerEstadisticasPorEstado());

        return "recepcion/dashboard";
    }

    // 2. Ver detalle de una reserva para validarla
    @GetMapping("/reservas/{id}")
    public String verDetalle(@PathVariable("id") Integer idReserva, Model model) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        model.addAttribute("reserva", reserva);
        return "recepcion/detalle";
    }

    // 3. Acción de Aprobar/Rechazar Pago
    @PostMapping("/pagos/gestionar")
    public String gestionarPago(
            @RequestParam("idPago") Integer idPago,
            @RequestParam("accion") String accion, // "APROBAR" o "RECHAZAR"
            @RequestParam("idReserva") Integer idReserva) {

        boolean aprobar = "APROBAR".equals(accion);
        pagoService.gestionarPago(idPago, aprobar);

        return "redirect:/recepcion/reservas/" + idReserva + "?gestionada=true";
    }

}