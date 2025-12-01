package com.elmirador.reservas.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "tipos_recurso")
public class TipoRecurso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer idTipo;

    private String nombre; // BUNGALOW, MESA, etc.
}