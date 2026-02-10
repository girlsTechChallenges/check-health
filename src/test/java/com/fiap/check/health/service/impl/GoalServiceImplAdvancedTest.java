package com.fiap.check.health.service.impl;

import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.api.model.ProgressRequest;
import com.fiap.check.health.event.publisher.GoalEventPublisher;
import com.fiap.check.health.exception.GoalNotFoundException;
import com.fiap.check.health.mapper.GoalMapper;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.model.Progress;
import com.fiap.check.health.persistence.entity.Goal;
import com.fiap.check.health.persistence.repository.GoalRepository;
import com.fiap.check.health.util.TestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes expandidos para GoalServiceImpl
 * 
 * Cenários adicionais incluem:
 * - Validação detalhada de lógica de negócio
 * - Cenários de edge cases
 * - Teste de transações
 * - Validação de gamificação
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoalService Implementation Advanced Tests")
class GoalServiceImplAdvancedTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private GoalEventPublisher goalEventPublisher;

    @InjectMocks
    private GoalServiceImpl goalService;

    private Goal goalEntity;
    private GoalRequest goalRequest;
    private GoalResponse goalResponse;

    @BeforeEach
    void setUp() {
        goalEntity = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
        goalRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
        goalResponse = TestDataFactory.GoalResponseBuilder.createValidGoalResponse();
    }

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Deve garantir que status seja sempre 'active' ao criar meta")
        void shouldEnsureStatusIsAlwaysActiveWhenCreatingGoal() {
            // Given
            Goal goalWithWrongStatus = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
            goalWithWrongStatus.setStatus("completed");

            when(goalMapper.toEntity(goalRequest)).thenReturn(goalWithWrongStatus);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalEntity);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> "active".equals(goal.getStatus())));
        }

        @Test
        @DisplayName("Deve definir createdAt automaticamente")
        void shouldSetCreatedAtAutomatically() {
            // Given
            when(goalMapper.toEntity(goalRequest)).thenReturn(goalEntity);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalEntity);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> goal.getCreatedAt() != null));
        }

        @Test
        @DisplayName("Deve atualizar apenas campos permitidos no updateGoal")
        void shouldUpdateOnlyAllowedFieldsInUpdateGoal() {
            // Given
            Goal existingGoal = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
            existingGoal.setGoalId(1L);
            existingGoal.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

            Goal updatedGoalData = Goal.builder()
                    .title("Novo Título")
                    .description("Nova Descrição")
                    .category(GoalCategory.SAUDE_MENTAL)
                    .build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(existingGoal));
            when(goalMapper.toEntity(goalRequest)).thenReturn(updatedGoalData);
            when(goalRepository.save(any(Goal.class))).thenReturn(existingGoal);
            when(goalMapper.toResponse(existingGoal)).thenReturn(goalResponse);

            // When
            goalService.updateGoal(1L, goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                goal.getGoalId().equals(1L) && // ID mantido
                goal.getCreatedAt().equals(LocalDateTime.of(2026, 1, 1, 10, 0)) // createdAt mantido
            ));
        }

        @Test
        @DisplayName("Deve preservar goalId e createdAt durante update")
        void shouldPreserveGoalIdAndCreatedAtDuringUpdate() {
            // Given
            LocalDateTime originalCreatedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
            Long originalGoalId = 123L;

            Goal existingGoal = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
            existingGoal.setGoalId(originalGoalId);
            existingGoal.setCreatedAt(originalCreatedAt);

            when(goalRepository.findById(originalGoalId)).thenReturn(Optional.of(existingGoal));
            when(goalMapper.toEntity(goalRequest)).thenReturn(TestDataFactory.GoalEntityBuilder.createValidGoalEntity());
            when(goalRepository.save(any(Goal.class))).thenReturn(existingGoal);
            when(goalMapper.toResponse(existingGoal)).thenReturn(goalResponse);

            // When
            goalService.updateGoal(originalGoalId, goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                goal.getGoalId().equals(originalGoalId) &&
                goal.getCreatedAt().equals(originalCreatedAt)
            ));
        }
    }

    @Nested
    @DisplayName("Progress Update Business Logic Tests")
    class ProgressUpdateBusinessLogicTests {

        @Test
        @DisplayName("Deve incrementar progresso corretamente")
        void shouldIncrementProgressCorrectly() {
            // Given
            Goal goalWithProgress = Goal.builder()
                    .goalId(1L)
                    .progress(Progress.builder().completed(5).total(30).build())
                    .build();

            ProgressRequest progressRequest = ProgressRequest.builder().increment(3).build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithProgress);
            when(goalMapper.toResponse(goalWithProgress)).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, progressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getCompleted() == 8 // 5 + 3
            ));
        }

        @Test
        @DisplayName("Deve marcar como 'completed' quando progresso atingir 100%")
        void shouldMarkAsCompletedWhenProgressReaches100Percent() {
            // Given
            Goal goalAlmostComplete = Goal.builder()
                    .goalId(1L)
                    .status("active")
                    .progress(Progress.builder().completed(28).total(30).build())
                    .build();

            ProgressRequest progressRequest = ProgressRequest.builder().increment(2).build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalAlmostComplete));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalAlmostComplete);
            when(goalMapper.toResponse(goalAlmostComplete)).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, progressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getCompleted() == 30 &&
                "completed".equals(goal.getStatus())
            ));
        }

        @Test
        @DisplayName("Deve marcar como 'completed' quando progresso ultrapassar 100%")
        void shouldMarkAsCompletedWhenProgressExceeds100Percent() {
            // Given
            Goal goalAlmostComplete = Goal.builder()
                    .goalId(1L)
                    .status("active")
                    .progress(Progress.builder().completed(29).total(30).build())
                    .build();

            ProgressRequest progressRequest = ProgressRequest.builder().increment(5).build(); // Vai dar 34/30

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalAlmostComplete));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalAlmostComplete);
            when(goalMapper.toResponse(goalAlmostComplete)).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, progressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getCompleted() == 34 &&
                "completed".equals(goal.getStatus())
            ));
        }

        @Test
        @DisplayName("Não deve alterar status se progresso for null")
        void shouldNotAlterStatusIfProgressIsNull() {
            // Given
            Goal goalWithoutProgress = Goal.builder()
                    .goalId(1L)
                    .status("active")
                    .progress(null)
                    .build();

            ProgressRequest progressRequest = ProgressRequest.builder().increment(5).build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithoutProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithoutProgress);
            when(goalMapper.toResponse(goalWithoutProgress)).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, progressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                "active".equals(goal.getStatus()) // Status deve permanecer inalterado
            ));
        }

        @Test
        @DisplayName("Deve lidar com incremento negativo")
        void shouldHandleNegativeIncrement() {
            // Given
            Goal goalWithProgress = Goal.builder()
                    .goalId(1L)
                    .progress(Progress.builder().completed(10).total(30).build())
                    .build();

            ProgressRequest negativeProgressRequest = ProgressRequest.builder().increment(-3).build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithProgress);
            when(goalMapper.toResponse(goalWithProgress)).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, negativeProgressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getCompleted() == 7 // 10 - 3
            ));
        }

        @Test
        @DisplayName("Deve lidar com incremento zero")
        void shouldHandleZeroIncrement() {
            // Given
            Goal goalWithProgress = Goal.builder()
                    .goalId(1L)
                    .progress(Progress.builder().completed(15).total(30).build())
                    .build();

            ProgressRequest zeroProgressRequest = ProgressRequest.builder().increment(0).build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithProgress);
            when(goalMapper.toResponse(goalWithProgress)).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, zeroProgressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getCompleted() == 15 // Deve permanecer igual
            ));
        }
    }

    @Nested
    @DisplayName("Event Publishing Edge Cases Tests")
    class EventPublishingEdgeCasesTests {

        @Test
        @DisplayName("Deve logar erro mas não falhar quando Kafka não conseguir publicar evento")
        void shouldLogErrorButNotFailWhenKafkaCannotPublishEvent() {
            // Given
            when(goalMapper.toEntity(goalRequest)).thenReturn(goalEntity);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalEntity);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);
            doThrow(new RuntimeException("Kafka indisponível")).when(goalEventPublisher).publishGoalCreated(goalEntity);

            // When & Then - Não deve lançar exceção
            GoalResponse result = goalService.createGoal(goalRequest);

            assertThat(result).isNotNull();
            verify(goalEventPublisher).publishGoalCreated(goalEntity);
            verify(goalRepository).save(any(Goal.class)); // Deve ter salvado mesmo assim
        }

        @Test
        @DisplayName("Deve tentar publicar evento mesmo se o goal tiver ID null")
        void shouldTryToPublishEventEvenIfGoalHasNullId() {
            // Given
            Goal goalWithNullId = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
            goalWithNullId.setGoalId(null);

            when(goalMapper.toEntity(goalRequest)).thenReturn(goalEntity);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithNullId);
            when(goalMapper.toResponse(goalWithNullId)).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalEventPublisher).publishGoalCreated(goalWithNullId);
        }
    }

    @Nested
    @DisplayName("Error Handling Edge Cases Tests")
    class ErrorHandlingEdgeCasesTests {

        @Test
        @DisplayName("Deve lançar exceção específica com ID correto quando goal não for encontrado para update")
        void shouldThrowSpecificExceptionWithCorrectIdWhenGoalNotFoundForUpdate() {
            // Given
            Long nonExistentId = 999L;
            when(goalRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> goalService.updateGoal(nonExistentId, goalRequest))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Deve lançar exceção específica com ID correto quando goal não for encontrado para deleteById")
        void shouldThrowSpecificExceptionWithCorrectIdWhenGoalNotFoundForDelete() {
            // Given
            Long nonExistentId = 888L;
            when(goalRepository.existsById(nonExistentId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> goalService.deleteGoal(nonExistentId))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("888");
        }

        @Test
        @DisplayName("Deve lançar exceção específica com ID correto quando goal não for encontrado para updateProgress")
        void shouldThrowSpecificExceptionWithCorrectIdWhenGoalNotFoundForUpdateProgress() {
            // Given
            Long nonExistentId = 777L;
            when(goalRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            ProgressRequest progressRequest = TestDataFactory.ProgressRequestBuilder.createValidProgressRequest();

            // When & Then
            assertThatThrownBy(() -> goalService.updateProgress(nonExistentId, progressRequest))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("777");
        }
    }

    @Nested
    @DisplayName("Transactional Behavior Tests")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Deve garantir que createGoal tenha comportamento transacional")
        void shouldEnsureCreateGoalHasTransactionalBehavior() {
            // Given
            when(goalMapper.toEntity(goalRequest)).thenReturn(goalEntity);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalEntity);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository, times(1)).save(any(Goal.class));
            verify(goalMapper, times(1)).toEntity(goalRequest);
            verify(goalMapper, times(1)).toResponse(goalEntity);
            verify(goalEventPublisher, times(1)).publishGoalCreated(goalEntity);
        }

        @Test
        @DisplayName("Deve garantir que listGoals tenha comportamento readOnly")
        void shouldEnsureListGoalsHasReadOnlyBehavior() {
            // Given
            List<Goal> goals = Arrays.asList(goalEntity);
            when(goalRepository.findAll()).thenReturn(goals);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            goalService.listGoals();

            // Then
            verify(goalRepository, times(1)).findAll();
            verify(goalRepository, never()).save(any());
            verify(goalRepository, never()).delete(any());
            verify(goalRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve garantir que findById tenha comportamento readOnly")
        void shouldEnsureFindByIdHasReadOnlyBehavior() {
            // Given
            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalEntity));
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            goalService.findById(1L);

            // Then
            verify(goalRepository, times(1)).findById(1L);
            verify(goalRepository, never()).save(any());
            verify(goalRepository, never()).delete(any());
            verify(goalRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Null Safety Tests")
    class EdgeCasesAndNullSafetyTests {

        @Test
        @DisplayName("Should handle invalid progress increment values gracefully")
        void shouldHandleInvalidProgressIncrements() {
            // Given
            Long goalId = 1L;
            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(-10) // Negative increment
                    .build();

            Goal existingGoal = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
            when(goalRepository.findById(goalId))
                    .thenReturn(Optional.of(existingGoal));
            when(goalRepository.save(any(Goal.class))).thenReturn(existingGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            GoalResponse response = goalService.updateProgress(goalId, progressRequest);

            // Then
            assertThat(response).isNotNull();
            verify(goalRepository).save(any(Goal.class));
        }

        @Test
        @DisplayName("Should handle very large progress increment values")
        void shouldHandleVeryLargeProgressIncrements() {
            // Given
            Long goalId = 1L;
            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(Integer.MAX_VALUE) // Very large increment
                    .build();

            Goal existingGoal = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
            when(goalRepository.findById(goalId))
                    .thenReturn(Optional.of(existingGoal));
            when(goalRepository.save(any(Goal.class))).thenReturn(existingGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            GoalResponse response = goalService.updateProgress(goalId, progressRequest);

            // Then
            assertThat(response).isNotNull();
            verify(goalRepository).save(any(Goal.class));
        }

        @Test
        @DisplayName("Should handle empty string descriptions in goal creation")
        void shouldHandleEmptyStringDescriptions() {
            // Given
            GoalRequest request = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
            request.setDescription(""); // Empty description

            Goal goal = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
            Goal savedGoal = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(goal);
            when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            GoalResponse result = goalService.createGoal(request);

            // Then
            assertThat(result).isNotNull();
            verify(goalRepository).save(any(Goal.class));
            verify(goalEventPublisher).publishGoalCreated(any(Goal.class));
        }
    }
}