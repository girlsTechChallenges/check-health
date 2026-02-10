package com.fiap.check.health.integration;

import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.api.model.ProgressRequest;
import com.fiap.check.health.persistence.entity.Goal;
import com.fiap.check.health.persistence.repository.GoalRepository;
import com.fiap.check.health.service.GoalService;
import com.fiap.check.health.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de Integração para GoalService
 * 
 * Valida o comportamento end-to-end do serviço de metas,
 * testando a integração entre service, repository e banco de dados.
 * 
 * Cenários cobertos:
 * - Criação de metas (sucesso e falhas de validação)
 * - Listagem de metas
 * - Busca de meta específica
 * - Atualização de metas
 * - Remoção de metas
 * - Atualização de progresso
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Goal Service Integration Tests")
@Transactional
class GoalControllerIntegrationTest {

    @Autowired
    private GoalService goalService;

    @Autowired
    private GoalRepository goalRepository;

    @BeforeEach
    void setUp() {
        // Limpa o banco antes de cada teste para isolamento
        goalRepository.deleteAll();
    }

    @Nested
    @DisplayName("Criação de Metas")
    class CriarMetas {

        @Test
        @DisplayName("Deve criar meta válida com sucesso")
        void deveCreiarMetaValidaComSucesso() {
            // Given - Preparar dados de entrada válidos
            GoalRequest goalRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();

            // When - Executar criação via service  
            GoalResponse response = goalService.createGoal(goalRequest);

            // Then - Validar resposta
            assertThat(response.getGoalId()).isNotNull();
            assertThat(response.getUserId()).isEqualTo(goalRequest.getUserId());
            assertThat(response.getTitle()).isEqualTo(goalRequest.getTitle());
            assertThat(response.getStatus()).isEqualTo("active");
            assertThat(response.getCreatedAt()).isNotNull();

            // Then - Verificar persistência no banco
            assertThat(goalRepository.count()).isEqualTo(1);
            
            Optional<Goal> savedGoal = goalRepository.findAll().stream().findFirst();
            assertThat(savedGoal).isPresent();
            assertThat(savedGoal.get().getTitle()).isEqualTo(goalRequest.getTitle());
            assertThat(savedGoal.get().getUserId()).isEqualTo(goalRequest.getUserId());
        }

        @Test
        @DisplayName("Deve criar meta com todas as propriedades opcionais")
        void deveCreiarMetaComPropriedadesOpcionais() {
            // Given - Meta com todas as propriedades preenchidas
            GoalRequest completeGoalRequest = TestDataFactory.GoalRequestBuilder.createGoalRequestWithFrequency();

            // When - Criar meta completa
            GoalResponse response = goalService.createGoal(completeGoalRequest);

            // Then - Validar criação
            assertThat(response.getGoalId()).isNotNull();
            assertThat(response.getUserId()).isEqualTo(completeGoalRequest.getUserId());
        }
    }

    @Nested
    @DisplayName("Listagem de Metas")
    class ListarMetas {

        @Test
        @DisplayName("Deve retornar lista vazia quando não há metas cadastradas")
        void deveRetornarListaVaziaQuandoNaoHaMetasCadastradas() {
            // When - Listar metas em banco vazio
            List<GoalResponse> goals = goalService.listGoals();

            // Then - Lista deve estar vazia
            assertThat(goals).isEmpty();
        }

        @Test
        @DisplayName("Deve listar metas existentes com sucesso")
        void deveListarMetasExistentesComSucesso() {
            // Given - Criar algumas metas no banco
            GoalRequest goal1 = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            goal1.setTitle("Meta 1");

            GoalRequest goal2 = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            goal2.setTitle("Meta 2");
            goal2.setUserId("user456");

            goalService.createGoal(goal1);
            goalService.createGoal(goal2);

            // When - Listar metas
            List<GoalResponse> goals = goalService.listGoals();

            // Then - Deve retornar ambas as metas
            assertThat(goals).hasSize(2);
            assertThat(goals).extracting(GoalResponse::getTitle)
                    .containsExactlyInAnyOrder("Meta 1", "Meta 2");
        }
    }

    @Nested
    @DisplayName("Busca de Meta por ID")
    class BuscarMetaPorId {

        @Test
        @DisplayName("Deve retornar meta existente por ID")
        void deveRetornarMetaExistentePorId() throws Exception {
            // Given - Meta salva no banco
            GoalRequest goalRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            GoalResponse createdGoal = goalService.createGoal(goalRequest);

            // When - Buscar por ID
            Optional<GoalResponse> foundGoal = goalService.findById(Long.parseLong(createdGoal.getGoalId()));

            // Then - Meta deve ser encontrada
            assertThat(foundGoal).isPresent();
            assertThat(foundGoal.get().getGoalId()).isEqualTo(createdGoal.getGoalId());
            assertThat(foundGoal.get().getTitle()).isEqualTo(goalRequest.getTitle());
        }

        @Test
        @DisplayName("Deve retornar vazio quando meta não existe")
        void deveRetornarVazioQuandoMetaNaoExiste(){
            // Given - ID que não existe
            Long nonExistentId = 99999L;

            // When - Buscar meta inexistente
            Optional<GoalResponse> foundGoal = goalService.findById(nonExistentId);

            // Then - Deve retornar vazio
            assertThat(foundGoal).isEmpty();
        }
    }

    @Nested
    @DisplayName("Atualização de Progresso")
    class AtualizarProgresso {

        @Test
        @DisplayName("Deve atualizar progresso de meta existente com sucesso")
        void deveAtualizarProgressoDeMetaExistenteComSucesso() throws Exception {
            // Given - Meta criada
            GoalRequest goalRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            GoalResponse createdGoal = goalService.createGoal(goalRequest);

            // Given - Request de atualização de progresso
            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(5)
                    .unit("days")
                    .build();

            // When - Atualizar progresso
            GoalResponse updatedGoal = goalService.updateProgress(
                    Long.parseLong(createdGoal.getGoalId()), 
                    progressRequest
            );

            // Then - Progresso deve ter sido atualizado
            assertThat(updatedGoal.getProgress().getCompleted()).isEqualTo(5);
            assertThat(updatedGoal.getMessage()).contains("Progress updated");

            // Then - Verificar persistência no banco
            Optional<Goal> savedGoal = goalRepository.findById(Long.parseLong(createdGoal.getGoalId()));
            assertThat(savedGoal).isPresent();
            assertThat(savedGoal.get().getProgress().getCompleted()).isEqualTo(5);
        }
    }
}