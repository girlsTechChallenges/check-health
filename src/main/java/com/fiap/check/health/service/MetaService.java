package com.fiap.check.health.service;

import com.fiap.check.health.exception.MetaNotFoundException;
import com.fiap.check.health.model.Meta;
import com.fiap.check.health.repository.MetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MetaService {

    private final MetaRepository metaRepository;

    public MetaService(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    @Transactional
    public Meta criarMeta(Meta meta) {
        meta.setDataCriacao(LocalDateTime.now());
        meta.setStatus("ativa");
        return metaRepository.save(meta);
    }

    @Transactional(readOnly = true)
    public List<Meta> listarMetas() {
        return metaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Meta> buscarPorId(Long metaId) {
        return metaRepository.findById(metaId);
    }

    @Transactional
    public Meta atualizarMeta(Long metaId, Meta metaAtualizada) {
        return metaRepository.findById(metaId)
                .map(meta -> {
                    meta.setTitulo(metaAtualizada.getTitulo());
                    meta.setDescricao(metaAtualizada.getDescricao());
                    meta.setCategoria(metaAtualizada.getCategoria());
                    meta.setTipo(metaAtualizada.getTipo());
                    meta.setDataInicio(metaAtualizada.getDataInicio());
                    meta.setDataFim(metaAtualizada.getDataFim());
                    meta.setFrequencia(metaAtualizada.getFrequencia());
                    meta.setDificuldade(metaAtualizada.getDificuldade());
                    meta.setRecompensa(metaAtualizada.getRecompensa());
                    meta.setStatus(metaAtualizada.getStatus());
                    meta.setNotificacoes(metaAtualizada.getNotificacoes());
                    return metaRepository.save(meta);
                })
                .orElseThrow(() -> new MetaNotFoundException(metaId));
    }

    @Transactional
    public void excluirMeta(Long metaId) {
        if (!metaRepository.existsById(metaId)) {
            throw new MetaNotFoundException(metaId);
        }
        metaRepository.deleteById(metaId);
    }

    @Transactional
    public Meta atualizarProgresso(Long metaId, int incremento) {
        return metaRepository.findById(metaId)
                .map(meta -> {
                    if (meta.getProgresso() != null) {
                        int concluido = meta.getProgresso().getConcluido() + incremento;
                        meta.getProgresso().setConcluido(concluido);
                        // lógica de gamificação: pontos extras, badges, etc.
                        if (concluido >= meta.getProgresso().getTotal()) {
                            meta.setStatus("concluída");
                        }
                    }
                    return metaRepository.save(meta);
                })
                .orElseThrow(() -> new MetaNotFoundException(metaId));
    }
}
