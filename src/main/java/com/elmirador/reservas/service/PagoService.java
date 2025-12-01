package com.elmirador.reservas.service;

import com.elmirador.reservas.model.Pago;
import com.elmirador.reservas.model.Reserva;
import com.elmirador.reservas.model.enums.EstadoPago;
import com.elmirador.reservas.model.enums.MetodoPago;
import com.elmirador.reservas.repository.PagoRepository;
import com.elmirador.reservas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;

    // Carpeta donde guardaremos las fotos de los vouchers
    private final Path rootLocation = Paths.get("uploads");

    @Transactional
    public void registrarPago(Integer idReserva, BigDecimal monto, MetodoPago metodo, String codigoOperacion,
            MultipartFile comprobante) throws IOException {

        // 1. Validar Reserva
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // VALIDACIÓN: Solo se puede pagar si está Pendiente
        if (reserva.getEstado() != com.elmirador.reservas.model.enums.EstadoReserva.PENDIENTE_PAGO) {
            throw new RuntimeException("La reserva no está en estado pendiente de pago.");
        }
        // 2. Guardar archivo (comprobante) si existe
        String nombreArchivo = null;
        if (comprobante != null && !comprobante.isEmpty()) {
            // Crear carpeta si no existe
            if (!Files.exists(rootLocation))
                Files.createDirectories(rootLocation);

            // Nombre único para no sobreescribir
            nombreArchivo = UUID.randomUUID().toString() + "_" + comprobante.getOriginalFilename();
            Files.copy(comprobante.getInputStream(), this.rootLocation.resolve(nombreArchivo));
        }

        // 3. Crear entidad Pago
        Pago nuevoPago = Pago.builder()
                .reserva(reserva)
                .monto(monto)
                .metodoPago(metodo)
                .codigoOperacion(codigoOperacion)
                .comprobanteImg(nombreArchivo)
                .estado(EstadoPago.PENDIENTE) // Nace pendiente de revisión
                .build();

        pagoRepository.save(nuevoPago);

        // Opcional: Si pagó el total con Tarjeta (simulado), podrías confirmar la
        // reserva automáticamente aquí.
        // Por ahora, dejamos que el Admin lo apruebe.
        // --- CAMBIO IMPORTANTE: Actualizar estado de Reserva ---
        reserva.setEstado(com.elmirador.reservas.model.enums.EstadoReserva.ESPERANDO_CONFIRMACION);
        reservaRepository.save(reserva);
    }

    // Agrega esto en PagoService.java

    @Transactional
    public void gestionarPago(Integer idPago, boolean aprobado) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        if (aprobado) {
            // 1. Aprobar el pago
            pago.setEstado(EstadoPago.APROBADO);

            // 2. Confirmar la reserva automáticamente
            Reserva reserva = pago.getReserva();
            reserva.setEstado(com.elmirador.reservas.model.enums.EstadoReserva.CONFIRMADA);
            reservaRepository.save(reserva); // Guardamos cambio en reserva
        } else {
            // Rechazar pago
            pago.setEstado(EstadoPago.RECHAZADO);
            // Opcional: Podrías cancelar la reserva o dejarla pendiente para que reintente
        }

        pagoRepository.save(pago); // Guardamos cambio en pago
    }
}