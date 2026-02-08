package com.fiap.check.health.repository;

import com.fiap.check.health.model.Meta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRepository extends JpaRepository<Meta, Long> {

    // Consultas adicionais podem ser definidas aqui
    // Exemplo: buscar metas por usuário
    List<Meta> findByUsuarioId(String usuarioId);

    // Exemplo: buscar metas por status
    List<Meta> findByStatus(String status);

    // Exemplo: buscar metas por categoria
    List<Meta> findByCategoria(String categoria);
}
