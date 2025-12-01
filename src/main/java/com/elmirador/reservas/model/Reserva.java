package com.elmirador.reservas.model;

import com.elmirador.reservas.model.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer idReserva;

    @Column(name = "codigo_reserva", unique = true, nullable = false)
    private String codigoReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recurso", nullable = false)
    private Recurso recurso;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "cantidad_personas")
    private Integer cantidadPersonas;

    @Column(name = "precio_total")
    private BigDecimal precioTotal;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    // Origen: WEB o PRESENCIAL. Lo dejo como String simple o puedes crear otro Enum
    private String origen;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relación inversa para poder hacer reserva.getPagos()
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<Pago> pagos;
}