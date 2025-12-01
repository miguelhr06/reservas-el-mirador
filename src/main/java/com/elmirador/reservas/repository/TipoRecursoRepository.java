package com.elmirador.reservas.repository;

import com.elmirador.reservas.model.TipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoRecursoRepository extends JpaRepository<TipoRecurso, Integer> {
}