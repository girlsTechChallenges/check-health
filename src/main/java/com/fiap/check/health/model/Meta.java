package com.fiap.check.health.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metaId;

    @Column(nullable = false)
    private String usuarioId;

    @Column(nullable = false)
    private String titulo;

    private String descricao;

    @Column(nullable = false)
    private String categoria; // saúde_física, saúde_mental, nutrição

    @Column(nullable = false)
    private String tipo; // diária, semanal, mensal, pontual

    @Column(nullable = false)
    private LocalDate dataInicio;

    private LocalDate dataFim;

    @Embedded
    private Frequencia frequencia;

    private String dificuldade; // fácil, média, difícil

    @Embedded
    private Recompensa recompensa;

    private String status; // ativa, concluída, arquivada

    private Boolean notificacoes;

    private LocalDateTime dataCriacao;

    @Embedded
    private Progresso progresso;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}
