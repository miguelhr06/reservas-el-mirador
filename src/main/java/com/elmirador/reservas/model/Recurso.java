package com.elmirador.reservas.model;

import com.elmirador.reservas.model.enums.EstadoRecurso;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "recursos")
public class Recurso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recurso")
    private Integer idRecurso;

    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Integer capacidad;

    @Column(name = "precio_base")
    private BigDecimal precioBase;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @ManyToOne
    @JoinColumn(name = "id_tipo")
    private TipoRecurso tipo;

    @Enumerated(EnumType.STRING)
    private EstadoRecurso estado;
    // ... otros atributos ...

    @Version // <--- LA MAGIA DE JPA
    private Long version;
}