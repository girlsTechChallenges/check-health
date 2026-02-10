package com.fiap.check.health.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.ProgressRequest;
import com.fiap.check.health.persistence.repository.GoalRepository;
import com.fiap.check.health.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Integração REST API para Goals
 * 
 * Objetivo: Validar end-to-end dos endpoints REST, incluindo:
 * - Serialização/deserialização JSON
 * - Validação de entrada 
 * - Códigos de status HTTP corretos
 * - Integração Controller + Service + Repository
 * - Comportamento em cenários de sucesso e falha
 * 
 * Usa MockMvc para simular requisições HTTP reais
 * sem subir servidor completo.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Goal REST API Integration Tests")
class GoalRestApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Configurar MockMvc com contexto Spring completo
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(print()) // Log das requisições para debug
                .build();
    }

    @Nested
    @DisplayName("POST /goals - Criar Meta")
    class CriarMetaEndpoint {

        @Test
        @DisplayName("Deve criar meta válida e retornar 201 Created com dados corretos")
        void deveCreiarMetaValidaComSucesso() throws Exception {
            // Given - Dados válidos para criação de meta
            GoalRequest request = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();

            // When & Then - Fazer POST /goals e validar resposta
            mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    
                    // Validar status HTTP
                    .andExpect(status().isCreated())
                    
                    // Validar Content-Type da resposta
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    
                    // Validar estrutura JSON da resposta
                    .andExpect(jsonPath("$.goalId").exists())
                    .andExpect(jsonPath("$.goalId").isNotEmpty())
                    .andExpect(jsonPath("$.userId").value(request.getUserId()))
                    .andExpect(jsonPath("$.title").value(request.getTitle()))
                    .andExpect(jsonPath("$.status").value("active"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    
                    // Validar campos de progresso inicializados
                    .andExpect(jsonPath("$.progress").exists())
                    .andExpect(jsonPath("$.progress.completed").value(0))
                    
                    // Validar resposta contém mensagem
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando dados são inválidos")
        void deveRetornar400QuandoDadosSaoInvalidos() throws Exception {
            // Given - Request com dados inválidos (título vazio)
            GoalRequest invalidRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            invalidRequest.setTitle(""); // título vazio - inválido
            invalidRequest.setUserId(null); // userId nulo - inválido

            // When & Then - POST com dados inválidos deve falhar
            mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    
                    // Deve retornar Bad Request
                    .andExpect(status().isBadRequest());
        }

        @Test  
        @DisplayName("Deve retornar 415 quando Content-Type não é JSON")
        void deveRetornar415QuandoContentTypeNaoEhJson() throws Exception {
            // Given - Request válido mas sem Content-Type correto
            GoalRequest request = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();

            // When & Then - POST sem Content-Type JSON
            mockMvc.perform(post("/goals")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(request)))
                    
                    // Deve retornar Unsupported Media Type
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("GET /goals - Listar Metas")
    class ListarMetasEndpoint {

        @Test
        @DisplayName("Deve retornar lista vazia quando não há metas")
        void deveRetornarListaVaziaQuandoNaoHaMetas() throws Exception {
            // When & Then - GET /goals em banco vazio
            mockMvc.perform(get("/goals")
                    .accept(MediaType.APPLICATION_JSON))
                    
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista com metas existentes")
        void deveRetornarListaComMetasExistentes() throws Exception {
            // Given - Criar algumas metas no sistema
            GoalRequest meta1 = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            meta1.setTitle("Meta de Exercícios");
            meta1.setUserId("user123");

            GoalRequest meta2 = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            meta2.setTitle("Meta de Alimentação");  
            meta2.setUserId("user456");

            // Criar metas através de POST para simular uso real
            mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(meta1)));

            mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(meta2)));

            // When & Then - GET /goals deve returnar ambas as metas
            mockMvc.perform(get("/goals")
                    .accept(MediaType.APPLICATION_JSON))
                    
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    
                    // Validar que ambas as metas estão na resposta
                    .andExpect(jsonPath("$[0].title").exists())
                    .andExpect(jsonPath("$[1].title").exists())
                    .andExpect(jsonPath("$[*].title", allOf(
                            hasItem("Meta de Exercícios"),
                            hasItem("Meta de Alimentação")
                    )));
        }
    }

    @Nested
    @DisplayName("GET /goals/{goalId} - Buscar Meta por ID")
    class BuscarMetaPorIdEndpoint {

        @Test
        @DisplayName("Deve retornar meta existente com todos os dados")
        void deveRetornarMetaExistenteComTodosOsDados() throws Exception {
            // Given - Criar uma meta primeiro
            GoalRequest request = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            
            String createResponse = mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            // Extrair goalId da resposta de criação
            String goalId = objectMapper
                    .readTree(createResponse)
                    .get("goalId")
                    .asText();

            // When & Then - GET /goals/{goalId}
            mockMvc.perform(get("/goals/{goalId}", goalId)
                    .accept(MediaType.APPLICATION_JSON))
                    
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.goalId").value(goalId))
                    .andExpect(jsonPath("$.userId").value(request.getUserId()))
                    .andExpect(jsonPath("$.title").value(request.getTitle()))
                    .andExpect(jsonPath("$.description").value(request.getDescription()))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.progress").exists());
        }

        @Test
        @DisplayName("Deve retornar 404 quando meta não existe")
        void deveRetornar404QuandoMetaNaoExiste() throws Exception {
            // Given - ID que não existe
            String nonExistentId = "99999";

            // When & Then - GET para meta inexistente
            mockMvc.perform(get("/goals/{goalId}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 quando ID tem formato inválido")
        void deveRetornar400QuandoIdTemFormatoInvalido() throws Exception {
            // Given - ID com formato inválido
            String invalidId = "abc123";

            // When & Then - GET com ID inválido
            mockMvc.perform(get("/goals/{goalId}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /goals/{goalId} - Atualizar Meta")  
    class AtualizarMetaEndpoint {

        @Test
        @DisplayName("Deve atualizar meta existente com sucesso")
        void deveAtualizarMetaExistenteComSucesso() throws Exception {
            // Given - Criar meta primeiro
            GoalRequest originalRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            
            String createResponse = mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(originalRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            String goalId = objectMapper
                    .readTree(createResponse)
                    .get("goalId")
                    .asText();

            // Given - Dados para atualização
            GoalRequest updateRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            updateRequest.setTitle("Meta Atualizada");
            updateRequest.setDescription("Descrição Atualizada");

            // When & Then - PUT /goals/{goalId}
            mockMvc.perform(put("/goals/{goalId}", goalId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.goalId").value(goalId))
                    .andExpect(jsonPath("$.title").value("Meta Atualizada"))
                    .andExpect(jsonPath("$.description").value("Descrição Atualizada"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando tentar atualizar meta inexistente")  
        void deveRetornar404QuandoTentarAtualizarMetaInexistente() throws Exception {
            // Given - Meta inexistente e dados válidos para update
            String nonExistentId = "99999";
            GoalRequest updateRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();

            // When & Then - PUT para meta inexistente
            mockMvc.perform(put("/goals/{goalId}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)  
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /goals/{goalId}/progress - Atualizar Progresso")
    class AtualizarProgressoEndpoint {

        @Test
        @DisplayName("Deve atualizar progresso de meta existente")
        void deveAtualizarProgressoDeMetaExistente() throws Exception {
            // Given - Criar meta primeiro
            GoalRequest request = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            
            String createResponse = mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            String goalId = objectMapper
                    .readTree(createResponse)
                    .get("goalId")
                    .asText();

            // Given - Request de progresso
            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(5)
                    .unit("days")
                    .build();

            // When & Then - PATCH /goals/{goalId}/progress
            mockMvc.perform(patch("/goals/{goalId}/progress", goalId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(progressRequest)))
                    
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.goalId").value(goalId))
                    .andExpect(jsonPath("$.progress.completed").value(5))
                    .andExpect(jsonPath("$.message", containsString("Progress updated")));
        }

        @Test
        @DisplayName("Deve retornar 404 quando tentar atualizar progresso de meta inexistente")
        void deveRetornar404QuandoTentarAtualizarProgressoDeMetaInexistente() throws Exception {
            // Given - Meta inexistente e request de progresso válido  
            String nonExistentId = "99999";
            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(3)
                    .unit("days")
                    .build();

            // When & Then - PATCH para meta inexistente
            mockMvc.perform(patch("/goals/{goalId}/progress", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(progressRequest)))
                    
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /goals/{goalId} - Deletar Meta")
    class DeletarMetaEndpoint {

        @Test
        @DisplayName("Deve deletar meta existente e retornar 204 No Content")
        void deveDeletarMetaExistenteComSucesso() throws Exception {
            // Given - Criar meta primeiro
            GoalRequest request = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            
            String createResponse = mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            String goalId = objectMapper
                    .readTree(createResponse)
                    .get("goalId")
                    .asText();

            // When & Then - DELETE /goals/{goalId}
            mockMvc.perform(delete("/goals/{goalId}", goalId))
                    .andExpect(status().isNoContent());

            // Then - Verificar que meta foi removida
            mockMvc.perform(get("/goals/{goalId}", goalId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 404 quando tentar deletar meta inexistente")
        void deveRetornar404QuandoTentarDeletarMetaInexistente() throws Exception {
            // Given - ID que não existe
            String nonExistentId = "99999";

            // When & Then - DELETE para meta inexistente
            mockMvc.perform(delete("/goals/{goalId}", nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }
}