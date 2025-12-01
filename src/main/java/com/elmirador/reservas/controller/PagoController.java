package com.elmirador.reservas.controller;

import com.elmirador.reservas.model.Reserva;
import com.elmirador.reservas.model.enums.MetodoPago;
import com.elmirador.reservas.repository.ReservaRepository;
import com.elmirador.reservas.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Controller
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final ReservaRepository reservaRepository;

    // 1. Mostrar Formulario de Pago
    // Solo debe haber UN método para esta ruta
    @GetMapping("/registrar/{id}")
    public String mostrarFormularioPago(@PathVariable("id") Integer idReserva, Model model) {
        // Buscamos la reserva para mostrar el monto y código en la vista
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        model.addAttribute("reserva", reserva);
        model.addAttribute("metodos", MetodoPago.values()); // Enviamos los métodos (Yape, Plin) al select

        return "pagos/registrar";
    }

    // 2. Procesar el Pago (Recibe los datos del formulario)
    @PostMapping("/guardar")
    public String guardarPago(
            @RequestParam("idReserva") Integer idReserva,
            @RequestParam("monto") BigDecimal monto,
            @RequestParam("metodoPago") MetodoPago metodoPago,
            @RequestParam("codigoOperacion") String codigoOperacion,
            @RequestParam("comprobante") MultipartFile comprobante,
            Model model) {

        try {
            pagoService.registrarPago(idReserva, monto, metodoPago, codigoOperacion, comprobante);
            // Redirigir con mensaje de éxito
            return "redirect:/reservas/mis-reservas?pagoExitoso";
        } catch (Exception e) {
            // Si falla, volvemos a cargar el formulario con el mensaje de error
            model.addAttribute("error", "Error al procesar pago: " + e.getMessage());

            // Necesitamos recargar la reserva y métodos para que la vista no se rompa
            Reserva reserva = reservaRepository.findById(idReserva).orElse(null);
            model.addAttribute("reserva", reserva);
            model.addAttribute("metodos", MetodoPago.values());

            return "pagos/registrar";
        }
    }
}