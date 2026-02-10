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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes End-to-End para Goals API
 * 
 * Objetivo: Testar workflows completos e cenários complexos que envolvem
 * múltiplas operações em sequência, simulando o uso real da API.
 * 
 * Cenários cobertos:
 * - Workflow completo: Criar → Listar → Atualizar → Atualizar Progresso → Deletar
 * - Múltiplas metas com diferentes usuários
 * - Validação de estados inconsistentes
 * - Comportamento de concorrência simulada
 * - Scenarios de negócio realistas
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Goals API End-to-End Integration Tests")
class GoalEndToEndIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(print())
                .build();
        
        goalRepository.deleteAll();
    }

    @Nested
    @DisplayName("Workflow Completo de Gerenciamento de Meta")
    class WorkflowCompletoGerenciamentoMeta {

        @Test
        @DisplayName("Cenário: Usuário cria meta, acompanha progresso e completa a meta")
        void cenarioCompletoDeVidaDeUmaMeta() throws Exception {
            // === ETAPA 1: CRIAR META ===
            GoalRequest novaMetaRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            novaMetaRequest.setTitle("Exercitar 30 dias seguidos");
            novaMetaRequest.setDescription("Meta de fazer exercícios todos os dias por 30 dias");
            novaMetaRequest.setUserId("usuario123");

            String createResponse = mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(novaMetaRequest)))
                    
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Exercitar 30 dias seguidos"))
                    .andExpect(jsonPath("$.status").value("active"))
                    .andExpect(jsonPath("$.progress.completed").value(0))
                    
                    .andReturn().getResponse().getContentAsString();

            // Extrair goalId de forma segura
            var jsonNode = objectMapper.readTree(createResponse);
            assertThat(jsonNode.get("goalId")).isNotNull();
            String goalId = jsonNode.get("goalId").asText();

            // === ETAPA 2: VERIFICAR META NA LISTAGEM ===
            mockMvc.perform(get("/goals")
                    .accept(MediaType.APPLICATION_JSON))
                    
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].goalId").value(goalId))
                    .andExpect(jsonPath("$[0].title").value("Exercitar 30 dias seguidos"));

            // === ETAPA 3: SIMULAR PROGRESSO DIÁRIO (múltiplas atualizações) ===
            // Dia 1-5: Progresso inicial
            for (int dia = 1; dia <= 5; dia++) {
                ProgressRequest progressRequest = ProgressRequest.builder()
                        .increment(1)
                        .unit("days")
                        .build();

                mockMvc.perform(patch("/goals/{goalId}/progress", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressRequest)))
                        
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.progress.completed").value(dia));
            }

            // === ETAPA 4: ATUALIZAR DESCRIÇÃO DA META ===
            GoalRequest updateRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            updateRequest.setTitle("Exercitar 30 dias seguidos");
            updateRequest.setDescription("Meta atualizada: Completando o desafio dos 30 dias!");
            updateRequest.setUserId("usuario123");

            mockMvc.perform(put("/goals/{goalId}", goalId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Meta atualizada: Completando o desafio dos 30 dias!"));

            // === ETAPA 5: CONTINUAR PROGRESSO ATÉ COMPLETAR ===
            // Dias 6-30: Completar meta
            for (int dia = 6; dia <= 30; dia++) {
                ProgressRequest progressRequest = ProgressRequest.builder()
                        .increment(1)
                        .unit("days")
                        .build();

                mockMvc.perform(patch("/goals/{goalId}/progress", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressRequest)))
                        
                        .andExpect(status().isOk());
            }

            // === ETAPA 6: VERIFICAR ESTADO FINAL DA META ===
            mockMvc.perform(get("/goals/{goalId}", goalId)
                    .accept(MediaType.APPLICATION_JSON))
                    
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.progress.completed").value(30))
                    .andExpect(jsonPath("$.status").value("completed"))
                    .andExpect(jsonPath("$.message", containsString("Congratulations")));

            // === ETAPA FINAL: DELETAR META CONCLUÍDA ===
            mockMvc.perform(delete("/goals/{goalId}", goalId))
                    .andExpect(status().isNoContent());

            // Verificar que a meta foi removida
            mockMvc.perform(get("/goals/{goalId}", goalId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Cenários com Múltiplas Metas e Usuários")
    class CenariosMultiplasMetasUsuarios {

        @Test
        @DisplayName("Cenário: Múltiplos usuários gerenciando suas metas independentemente")
        void cenarioMultiplosUsuariosGerenciandoMetasIndependentemente() throws Exception {
            // === USUÁRIO 1: Criar meta de exercícios ===
            GoalRequest metaUsuario1 = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            metaUsuario1.setTitle("Correr 5km diariamente");
            metaUsuario1.setUserId("usuario1");

            String responseUsuario1 = mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(metaUsuario1)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            // Extrair goalId de forma segura
            var jsonNode1 = objectMapper.readTree(responseUsuario1);
            assertThat(jsonNode1.get("goalId")).isNotNull();
            String goalIdUsuario1 = jsonNode1.get("goalId").asText();

            // === USUÁRIO 2: Criar meta de alimentação ===
            GoalRequest metaUsuario2 = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            metaUsuario2.setTitle("Comer 5 frutas por dia");
            metaUsuario2.setUserId("usuario2");

            String responseUsuario2 = mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(metaUsuario2)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            // Extrair goalId de forma segura
            var jsonNode2 = objectMapper.readTree(responseUsuario2);
            assertThat(jsonNode2.get("goalId")).isNotNull();
            String goalIdUsuario2 = jsonNode2.get("goalId").asText();

            // === USUÁRIO 1: Progresso em sua meta ===
            ProgressRequest progressoUsuario1 = ProgressRequest.builder()
                    .increment(3)
                    .unit("days")
                    .build();

            mockMvc.perform(patch("/goals/{goalId}/progress", goalIdUsuario1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(progressoUsuario1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.progress.completed").value(3));

            // === USUÁRIO 2: Progresso diferente em sua meta ===
            ProgressRequest progressoUsuario2 = ProgressRequest.builder()
                    .increment(7)
                    .unit("days")
                    .build();

            mockMvc.perform(patch("/goals/{goalId}/progress", goalIdUsuario2)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(progressoUsuario2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.progress.completed").value(7));

            // === VERIFICAR LISTAGEM CONTÉM AMBAS AS METAS ===
            mockMvc.perform(get("/goals")
                    .accept(MediaType.APPLICATION_JSON))
                    
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].title", allOf(
                            hasItem("Correr 5km diariamente"),
                            hasItem("Comer 5 frutas por dia")
                    )))
                    .andExpect(jsonPath("$[*].userId", allOf(
                            hasItem("usuario1"),
                            hasItem("usuario2")
                    )));

            // === VERIFICAR PROGRESSO INDEPENDENTE ===
            mockMvc.perform(get("/goals/{goalId}", goalIdUsuario1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.progress.completed").value(3))
                    .andExpect(jsonPath("$.userId").value("usuario1"));

            mockMvc.perform(get("/goals/{goalId}", goalIdUsuario2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.progress.completed").value(7))
                    .andExpect(jsonPath("$.userId").value("usuario2"));
        }

        @Test
        @DisplayName("Cenário: Usuário com múltiplas metas ativas simultaneamente")
        void cenarioUsuarioComMultiplasMetasAtivas() throws Exception {
            String userId = "usuarioMultiplasMetasAtivas";

            // Criar 3 metas diferentes para o mesmo usuário
            String[] tiposMetas = {
                "Meta de Exercícios: 30min diários",
                "Meta de Leitura: 1 livro por mês", 
                "Meta de Água: 2L por dia"
            };

            String[] goalIds = new String[3];

            for (int i = 0; i < tiposMetas.length; i++) {
                GoalRequest meta = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
                meta.setTitle(tiposMetas[i]);
                meta.setUserId(userId);

                String response = mockMvc.perform(post("/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meta)))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();

                goalIds[i] = objectMapper.readTree(response).get("goalId").asText();
            }

            // Verificar que todas as 3 metas foram criadas
            mockMvc.perform(get("/goals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].userId", allOf(
                            hasItem(userId), hasItem(userId), hasItem(userId)
                    )));

            // Simular progresso diferente em cada meta
            int[] progressos = {5, 2, 10}; // Progressos diferentes para cada meta

            for (int i = 0; i < goalIds.length; i++) {
                ProgressRequest progress = ProgressRequest.builder()
                        .increment(progressos[i])
                        .unit("days")
                        .build();

                mockMvc.perform(patch("/goals/{goalId}/progress", goalIds[i])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progress)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.progress.completed").value(progressos[i]));
            }

            // Verificar estado final de cada meta
            for (int i = 0; i < goalIds.length; i++) {
                mockMvc.perform(get("/goals/{goalId}", goalIds[i]))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value(tiposMetas[i]))
                        .andExpect(jsonPath("$.userId").value(userId))
                        .andExpect(jsonPath("$.progress.completed").value(progressos[i]));
            }
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Edge Cases")
    class CenariosValidacaoEdgeCases {

        @Test
        @DisplayName("Cenário: Tentativas de operações inválidas em sequência")
        void cenarioTentativasOperacoesInvidasEmSequencia() throws Exception {
            // Criar uma meta válida primeiro
            GoalRequest metaValida = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            
            String createResponse = mockMvc.perform(post("/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(metaValida)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String goalIdValido = objectMapper.readTree(createResponse).get("goalId").asText();

            // === TESTE 1: Tentar buscar meta com ID inválido ===
            mockMvc.perform(get("/goals/abc123"))
                    .andExpect(status().isBadRequest());

            // === TESTE 2: Tentar atualizar meta inexistente ===
            mockMvc.perform(put("/goals/99999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(metaValida)))
                    .andExpect(status().isNotFound());

            // === TESTE 3: Tentar atualizar progresso com dados inválidos ===
            ProgressRequest progressoInvalido = ProgressRequest.builder()
                    .increment(-5) // Valor negativo
                    .unit("invalidUnit")
                    .build();

            mockMvc.perform(patch("/goals/{goalId}/progress", goalIdValido)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(progressoInvalido)))
                    .andExpect(status().isOk()); // Service deve tratar valores negativos

            // === TESTE 4: Tentar deletar meta inexistente ===
            mockMvc.perform(delete("/goals/99999"))
                    .andExpect(status().isNotFound());

            // === TESTE 5: Verificar que a meta válida ainda existe após operações inválidas ===
            mockMvc.perform(get("/goals/{goalId}", goalIdValido))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.goalId").value(goalIdValido));
        }

        @Test
        @DisplayName("Cenário: Stress test com criação e deleção em massa")
        void cenarioStressTestCriacaoDelecaoEmMassa() throws Exception {
            final int NUM_METAS = 10;
            String[] goalIds = new String[NUM_METAS];

            // === CRIAÇÃO EM MASSA ===
            for (int i = 0; i < NUM_METAS; i++) {
                GoalRequest meta = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
                meta.setTitle("Meta Stress Test " + i);
                meta.setUserId("usuarioStressTest" + (i % 3)); // 3 usuários diferentes

                String response = mockMvc.perform(post("/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meta)))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString();

                goalIds[i] = objectMapper.readTree(response).get("goalId").asText();
            }

            // === VERIFICAÇÃO: Todas foram criadas ===
            mockMvc.perform(get("/goals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(NUM_METAS)));

            // === DELEÇÃO EM MASSA (metade) ===
            for (int i = 0; i < NUM_METAS / 2; i++) {
                mockMvc.perform(delete("/goals/{goalId}", goalIds[i]))
                        .andExpect(status().isNoContent());
            }

            // === VERIFICAÇÃO: Metade foi deletada ===
            mockMvc.perform(get("/goals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(NUM_METAS / 2)));

            // === VERIFICAÇÃO: Metas deletadas não existem mais ===
            for (int i = 0; i < NUM_METAS / 2; i++) {
                mockMvc.perform(get("/goals/{goalId}", goalIds[i]))
                        .andExpect(status().isNotFound());
            }

            // === VERIFICAÇÃO: Metas restantes ainda existem ===
            for (int i = NUM_METAS / 2; i < NUM_METAS; i++) {
                mockMvc.perform(get("/goals/{goalId}", goalIds[i]))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value("Meta Stress Test " + i));
            }
        }
    }
}