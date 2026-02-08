package com.fiap.check.health.mapper;

import com.fiap.check.health.api.model.*;
import com.fiap.check.health.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class MetaMapper {

    public Meta toEntity(MetaRequest request) {
        if (request == null) {
            return null;
        }

        return Meta.builder()
                .usuarioId(request.getUsuarioId())
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .categoria(request.getCategoria() != null ? request.getCategoria().getValue() : null)
                .tipo(request.getTipo() != null ? request.getTipo().getValue() : null)
                .dataInicio(request.getDataInicio())
                .dataFim(request.getDataFim())
                .frequencia(toFrequenciaEntity(request.getFrequencia()))
                .dificuldade(request.getDificuldade() != null ? request.getDificuldade().getValue() : null)
                .recompensa(toRecompensaEntity(request.getRecompensa()))
                .status(request.getStatus() != null ? request.getStatus().getValue() : null)
                .notificacoes(request.getNotificacoes())
                .build();
    }

    public MetaResponse toResponse(Meta meta) {
        if (meta == null) {
            return null;
        }

        MetaResponse.MetaResponseBuilder builder = MetaResponse.builder();
        
        builder.metaId(meta.getMetaId() != null ? meta.getMetaId().toString() : null);
        builder.usuarioId(meta.getUsuarioId());
        builder.titulo(meta.getTitulo());
        builder.status(meta.getStatus());
        
        if (meta.getDataCriacao() != null) {
            builder.dataCriacao(OffsetDateTime.of(meta.getDataCriacao(), ZoneOffset.UTC));
        }
        
        builder.progresso(toProgressoResponse(meta.getProgresso()));
        builder.gamificacao(toGamificacaoResponse(meta));
        
        // Mensagem de progresso
        if (meta.getProgresso() != null) {
            int concluido = meta.getProgresso().getConcluido() != null ? meta.getProgresso().getConcluido() : 0;
            int total = meta.getProgresso().getTotal() != null ? meta.getProgresso().getTotal() : 0;
            int pontos = meta.getRecompensa() != null && meta.getRecompensa().getPontos() != null ? 
                    meta.getRecompensa().getPontos() : 0;
            
            String mensagem = String.format(
                "Progresso atualizado! Você completou %d de %d %s e ganhou %d pontos.",
                concluido, total, meta.getProgresso().getUnidade() != null ? meta.getProgresso().getUnidade() : "dias", pontos
            );
            builder.mensagem(mensagem);
        }

        return builder.build();
    }

    private Frequencia toFrequenciaEntity(MetaRequestFrequencia dto) {
        if (dto == null) {
            return null;
        }

        return Frequencia.builder()
                .periodicidade(dto.getPeriodicidade())
                .vezesPorPeriodo(dto.getVezesPorPeriodo())
                .build();
    }

    private Recompensa toRecompensaEntity(MetaRequestRecompensa dto) {
        if (dto == null) {
            return null;
        }

        return Recompensa.builder()
                .pontos(dto.getPontos())
                .badge(dto.getBadge())
                .build();
    }

    private MetaResponseProgresso toProgressoResponse(Progresso progresso) {
        if (progresso == null) {
            return null;
        }

        return MetaResponseProgresso.builder()
                .concluido(progresso.getConcluido())
                .total(progresso.getTotal())
                .unidade(progresso.getUnidade())
                .build();
    }

    private MetaResponseGamificacao toGamificacaoResponse(Meta meta) {
        Integer pontosAtribuidos = null;
        String badge = null;
        
        if (meta.getRecompensa() != null) {
            pontosAtribuidos = meta.getRecompensa().getPontos();
            badge = meta.getRecompensa().getBadge();
        }
        
        // Nível do usuário poderia ser calculado com base no total de pontos
        // Por enquanto, retornando um valor fixo
        return MetaResponseGamificacao.builder()
                .pontosAtribuidos(pontosAtribuidos)
                .badge(badge)
                .nivelUsuario(1)
                .build();
    }
}
