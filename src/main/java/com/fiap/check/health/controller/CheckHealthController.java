package com.fiap.check.health.controller;

import com.fiap.check.health.api.CheckHealthApi;
import com.fiap.check.health.api.model.MetaRequest;
import com.fiap.check.health.api.model.MetaResponse;
import com.fiap.check.health.api.model.ProgressoRequest;
import com.fiap.check.health.mapper.MetaMapper;
import com.fiap.check.health.model.Meta;
import com.fiap.check.health.service.MetaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CheckHealthController implements CheckHealthApi {

    private final MetaService metaService;
    private final MetaMapper metaMapper;

    public CheckHealthController(MetaService metaService, MetaMapper metaMapper) {
        this.metaService = metaService;
        this.metaMapper = metaMapper;
    }

    @Override
    public ResponseEntity<MetaResponse> metasPost(@Valid MetaRequest metaRequest) {
        Meta meta = metaMapper.toEntity(metaRequest);
        Meta novaMeta = metaService.criarMeta(meta);
        MetaResponse response = metaMapper.toResponse(novaMeta);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<MetaResponse>> metasGet() {
        List<Meta> metas = metaService.listarMetas();
        List<MetaResponse> responses = metas.stream()
                .map(metaMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<MetaResponse> metasMetaIdGet(String metaId) {
        return metaService.buscarPorId(Long.parseLong(metaId))
                .map(metaMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<MetaResponse> metasMetaIdPut(String metaId, @Valid MetaRequest metaRequest) {
        Meta metaAtualizada = metaMapper.toEntity(metaRequest);
        Meta meta = metaService.atualizarMeta(Long.parseLong(metaId), metaAtualizada);
        MetaResponse response = metaMapper.toResponse(meta);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> metasMetaIdDelete(String metaId) {
        metaService.excluirMeta(Long.parseLong(metaId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MetaResponse> metasMetaIdProgressoPatch(String metaId, @Valid ProgressoRequest progressoRequest) {
        Meta meta = metaService.atualizarProgresso(
                Long.parseLong(metaId), 
                progressoRequest.getIncremento()
        );
        MetaResponse response = metaMapper.toResponse(meta);
        return ResponseEntity.ok(response);
    }
}
