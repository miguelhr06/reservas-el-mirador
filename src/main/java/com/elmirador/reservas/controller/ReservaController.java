package com.elmirador.reservas.controller;

import com.elmirador.reservas.model.Recurso;
import com.elmirador.reservas.model.Reserva;
import com.elmirador.reservas.service.RecursoService;
import com.elmirador.reservas.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;
    private final RecursoService recursoService;

    // 1. Mostrar pantalla de búsqueda (Fechas)
    @GetMapping("/buscar")
    public String mostrarBuscador() {
        return "reservas/buscar";
    }

    // 2. Procesar búsqueda y mostrar resultados
    @GetMapping("/resultados")
    public String buscarDisponibilidad(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Model model) {

        try {
            // Aquí se ejecutarán las validaciones del Service
            List<Recurso> disponibles = recursoService.buscarDisponibles(fechaInicio, fechaFin);

            model.addAttribute("recursos", disponibles);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            return "reservas/resultados";

        } catch (IllegalArgumentException e) {
            // CAPTURAMOS LAS RESTRICCIONES (Validaciones de negocio)
            model.addAttribute("error", e.getMessage());

            // IMPORTANTE: Devolvemos a la vista "buscar", no "resultados"
            return "reservas/buscar";

        } catch (Exception e) {
            // Otros errores técnicos
            model.addAttribute("error", "Ocurrió un error inesperado: " + e.getMessage());
            return "reservas/buscar";
        }
    }

    // 3. Pre-confirmación (Ver detalles y precio total antes de guardar)
    @GetMapping("/confirmar/{id}")
    public String preConfirmarReserva(
            @PathVariable("id") Integer idRecurso,
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Model model) {

        Recurso recurso = recursoService.obtenerPorId(idRecurso);

        // Calcular precio total para mostrarlo en pantalla
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (dias < 1)
            dias = 1;
        BigDecimal total = recurso.getPrecioBase().multiply(new BigDecimal(dias));

        model.addAttribute("recurso", recurso);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("dias", dias);
        model.addAttribute("total", total);

        return "reservas/confirmar";
    }

    // Agrega RedirectAttributes en los argumentos
    @PostMapping("/crear")
    public String crearReserva(
            @RequestParam("idRecurso") Integer idRecurso,
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam("cantidadPersonas") Integer cantidadPersonas,
            Principal principal,
            // CAMBIO AQUÍ: Usamos RedirectAttributes en vez de Model para redirecciones
            RedirectAttributes redirectAttributes,
            Model model // Mantenemos Model por si caemos en el catch de concurrencia (que no redirige)
    ) {

        try {
            String emailUsuario = principal.getName();
            reservaService.crearReserva(emailUsuario, idRecurso, fechaInicio, fechaFin, cantidadPersonas);
            return "redirect:/reservas/mis-reservas?exito";

        } catch (Exception e) {
            // Ahora capturamos todo aquí.
            // Si es el usuario B (el que perdió), el mensaje será "Lo sentimos, este
            // recurso ya no está disponible."
            // Usamos redirectAttributes para que sobreviva a la redirección.
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            // Redirigimos a BUSCAR en vez de resultados, porque la búsqueda original ya
            // venció
            return "redirect:/reservas/buscar";
        }
    }

    // Agrega esto en ReservaController.java

    @GetMapping("/mis-reservas")
    public String listarMisReservas(Model model, java.security.Principal principal) {
        String email = principal.getName();
        List<Reserva> misReservas = reservaService.listarReservasPorUsuario(email);

        model.addAttribute("reservas", misReservas);
        return "reservas/mis-reservas";
    }

    // 1. Cancelar Reserva
    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Integer id, Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            reservaService.cancelarReservaUsuario(id, principal.getName());
            redirectAttributes.addFlashAttribute("exito", "Reserva cancelada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservas/mis-reservas";
    }

    // 2. Ver Detalle
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Integer id, Model model, Principal principal) {
        // Reutilizamos la lógica de buscar (podrías agregar validación de usuario aquí
        // también)
        // Por rapidez usamos el repositorio, idealmente úsalo via Service
        Reserva reserva = reservaService.buscarPorId(id); // Asegúrate de tener este método en Service

        // Validación de seguridad simple
        if (!reserva.getUsuario().getEmail().equals(principal.getName())) {
            return "redirect:/reservas/mis-reservas?error=AccesoDenegado";
        }

        model.addAttribute("reserva", reserva);
        return "reservas/detalle-cliente";
    }
}