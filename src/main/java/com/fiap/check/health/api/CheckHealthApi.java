/**
 * Interface da API Check Health.
 * Baseada na especificação OpenAPI check-health-api.yml
 */
package com.fiap.check.health.api;

import com.fiap.check.health.api.model.MetaRequest;
import com.fiap.check.health.api.model.MetaResponse;
import com.fiap.check.health.api.model.ProgressoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Validated
@Tag(name = "Check Health", description = "API de Metas Gamificadas")
public interface CheckHealthApi {

    /**
     * GET /metas : Listar todas as metas
     *
     * @return Lista de metas (status code 200)
     */
    @Operation(
        operationId = "metasGet",
        summary = "Listar todas as metas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de metas", content = {
                @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MetaResponse.class)))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/metas",
        produces = { "application/json" }
    )
    
    ResponseEntity<List<MetaResponse>> metasGet();


    /**
     * DELETE /metas/{meta_id} : Excluir uma meta
     *
     * @param metaId  (required)
     * @return Meta excluída com sucesso (status code 204)
     *         or Meta não encontrada (status code 404)
     */
    @Operation(
        operationId = "metasMetaIdDelete",
        summary = "Excluir uma meta",
        responses = {
            @ApiResponse(responseCode = "204", description = "Meta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada")
        }
    )
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/metas/{meta_id}"
    )
    
    ResponseEntity<Void> metasMetaIdDelete(
        @Parameter(name = "meta_id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("meta_id") String metaId
    );


    /**
     * GET /metas/{meta_id} : Obter detalhes de uma meta
     *
     * @param metaId  (required)
     * @return Detalhes da meta (status code 200)
     *         or Meta não encontrada (status code 404)
     */
    @Operation(
        operationId = "metasMetaIdGet",
        summary = "Obter detalhes de uma meta",
        responses = {
            @ApiResponse(responseCode = "200", description = "Detalhes da meta", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = MetaResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada")
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/metas/{meta_id}",
        produces = { "application/json" }
    )
    
    ResponseEntity<MetaResponse> metasMetaIdGet(
        @Parameter(name = "meta_id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("meta_id") String metaId
    );


    /**
     * PATCH /metas/{meta_id}/progresso : Atualizar progresso de uma meta
     * Registra conquistas parciais e atualiza recompensas.
     *
     * @param metaId  (required)
     * @param progressoRequest  (required)
     * @return Progresso atualizado com sucesso (status code 200)
     *         or Meta não encontrada (status code 404)
     */
    @Operation(
        operationId = "metasMetaIdProgressoPatch",
        summary = "Atualizar progresso de uma meta",
        description = "Registra conquistas parciais e atualiza recompensas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Progresso atualizado com sucesso", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = MetaResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada")
        }
    )
    @RequestMapping(
        method = RequestMethod.PATCH,
        value = "/metas/{meta_id}/progresso",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    ResponseEntity<MetaResponse> metasMetaIdProgressoPatch(
        @Parameter(name = "meta_id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("meta_id") String metaId,
        @Parameter(name = "ProgressoRequest", description = "", required = true) @Valid @RequestBody ProgressoRequest progressoRequest
    );


    /**
     * PUT /metas/{meta_id} : Atualizar uma meta existente
     *
     * @param metaId  (required)
     * @param metaRequest  (required)
     * @return Meta atualizada com sucesso (status code 200)
     *         or Meta não encontrada (status code 404)
     */
    @Operation(
        operationId = "metasMetaIdPut",
        summary = "Atualizar uma meta existente",
        responses = {
            @ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = MetaResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada")
        }
    )
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/metas/{meta_id}",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    ResponseEntity<MetaResponse> metasMetaIdPut(
        @Parameter(name = "meta_id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("meta_id") String metaId,
        @Parameter(name = "MetaRequest", description = "", required = true) @Valid @RequestBody MetaRequest metaRequest
    );


    /**
     * POST /metas : Criar uma nova meta
     *
     * @param metaRequest  (required)
     * @return Meta criada com sucesso (status code 201)
     */
    @Operation(
        operationId = "metasPost",
        summary = "Criar uma nova meta",
        responses = {
            @ApiResponse(responseCode = "201", description = "Meta criada com sucesso", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = MetaResponse.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/metas",
        produces = { "application/json" },
        consumes = { "application/json" }
    )
    
    ResponseEntity<MetaResponse> metasPost(
        @Parameter(name = "MetaRequest", description = "", required = true) @Valid @RequestBody MetaRequest metaRequest
    );

}
