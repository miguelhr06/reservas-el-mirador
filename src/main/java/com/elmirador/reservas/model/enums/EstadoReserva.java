package com.elmirador.reservas.model.enums;

public enum EstadoReserva {
    PENDIENTE_PAGO,
    ESPERANDO_CONFIRMACION, // <--- NUEVO
    CONFIRMADA,
    COMPLETADA,
    CANCELADA,
    NO_SHOW
}
