package com.elmirador.reservas.repository;

import com.elmirador.reservas.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    // Ver todos los pagos de una reserva específica
    List<Pago> findByReservaIdReserva(Integer idReserva);
}