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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalService Implementation Tests")
class GoalServiceImplTest {

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
    private ProgressRequest progressRequest;

    @BeforeEach
    void setUp() {
        // Dados de exemplo para os testes
        goalEntity = Goal.builder()
                .goalId(1L)
                .userId("user123")
                .title("Exercitar-se diariamente")
                .description("Fazer 30 minutos de exercícios por dia")
                .category(GoalCategory.SAUDE_FISICA)
                .type("daily")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status("active")
                .notifications(true)
                .createdAt(LocalDateTime.now())
                .progress(Progress.builder().completed(0).total(30).build())
                .build();

        goalRequest = GoalRequest.builder()
                .userId("user123")
                .title("Exercitar-se diariamente")
                .description("Fazer 30 minutos de exercícios por dia")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .notifications(true)
                .build();

        goalResponse = GoalResponse.builder()
                .goalId("1")
                .userId("user123")
                .title("Exercitar-se diariamente")
                .status("active")
                .build();

        progressRequest = ProgressRequest.builder()
                .increment(1)
                .build();
    }

    @Nested
    @DisplayName("Create Goal Tests")
    class CreateGoalTests {

        @Test
        @DisplayName("Deve criar uma meta com sucesso")
        void shouldCreateGoalSuccessfully() {
            // Given
            when(goalMapper.toEntity(goalRequest)).thenReturn(goalEntity);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalEntity);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            GoalResponse result = goalService.createGoal(goalRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo("user123");
            assertThat(result.getTitle()).isEqualTo("Exercitar-se diariamente");
            
            verify(goalMapper).toEntity(goalRequest);
            verify(goalRepository).save(any(Goal.class));
            verify(goalMapper).toResponse(goalEntity);
            verify(goalEventPublisher).publishGoalCreated(goalEntity);
        }

        @Test
        @DisplayName("Deve definir status como 'active' ao criar meta")
        void shouldSetStatusAsActiveWhenCreatingGoal() {
            // Given
            Goal goalToSave = Goal.builder().build();
            when(goalMapper.toEntity(goalRequest)).thenReturn(goalToSave);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalEntity);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> 
                "active".equals(goal.getStatus()) && goal.getCreatedAt() != null
            ));
        }

        @Test
        @DisplayName("Deve continuar funcionando mesmo se falhar ao publicar evento Kafka")
        void shouldContinueWorkingEvenIfKafkaEventPublishingFails() {
            // Given
            when(goalMapper.toEntity(goalRequest)).thenReturn(goalEntity);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalEntity);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);
            doThrow(new RuntimeException("Kafka error")).when(goalEventPublisher).publishGoalCreated(goalEntity);

            // When & Then
            GoalResponse result = goalService.createGoal(goalRequest);
            
            assertThat(result).isNotNull();
            verify(goalEventPublisher).publishGoalCreated(goalEntity);
        }
    }

    @Nested
    @DisplayName("List Goals Tests")
    class ListGoalsTests {

        @Test
        @DisplayName("Deve retornar lista de metas com sucesso")
        void shouldReturnListOfGoalsSuccessfully() {
            // Given
            List<Goal> goalEntities = Arrays.asList(goalEntity);
            when(goalRepository.findAll()).thenReturn(goalEntities);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            List<GoalResponse> result = goalService.listGoals();

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo("user123");
            
            verify(goalRepository).findAll();
            verify(goalMapper).toResponse(goalEntity);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há metas")
        void shouldReturnEmptyListWhenNoGoalsExist() {
            // Given
            when(goalRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<GoalResponse> result = goalService.listGoals();

            // Then
            assertThat(result).isEmpty();
            verify(goalRepository).findAll();
            verify(goalMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Find By Id Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Deve encontrar meta por ID com sucesso")
        void shouldFindGoalByIdSuccessfully() {
            // Given
            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalEntity));
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            Optional<GoalResponse> result = goalService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo("user123");
            
            verify(goalRepository).findById(1L);
            verify(goalMapper).toResponse(goalEntity);
        }

        @Test
        @DisplayName("Deve retornar Optional vazio quando meta não for encontrada")
        void shouldReturnEmptyOptionalWhenGoalNotFound() {
            // Given
            when(goalRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<GoalResponse> result = goalService.findById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(goalRepository).findById(999L);
            verify(goalMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Update Goal Tests")
    class UpdateGoalTests {

        @Test
        @DisplayName("Deve atualizar meta com sucesso")
        void shouldUpdateGoalSuccessfully() {
            // Given
            Goal updatedGoalEntity = Goal.builder()
                    .title("Novo título")
                    .description("Nova descrição")
                    .build();
            
            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalEntity));
            when(goalMapper.toEntity(goalRequest)).thenReturn(updatedGoalEntity);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalEntity);
            when(goalMapper.toResponse(goalEntity)).thenReturn(goalResponse);

            // When
            GoalResponse result = goalService.updateGoal(1L, goalRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo("user123");
            
            verify(goalRepository).findById(1L);
            verify(goalMapper).toEntity(goalRequest);
            verify(goalRepository).save(goalEntity);
            verify(goalMapper).toResponse(goalEntity);
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar meta inexistente")
        void shouldThrowExceptionWhenUpdatingNonExistentGoal() {
            // Given
            when(goalRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> goalService.updateGoal(999L, goalRequest))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("Goal não encontrado com ID: 999");
            
            verify(goalRepository).findById(999L);
            verify(goalMapper, never()).toEntity(any());
            verify(goalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Goal Tests")
    class DeleteGoalTests {

        @Test
        @DisplayName("Deve deletar meta com sucesso")
        void shouldDeleteGoalSuccessfully() {
            // Given
            when(goalRepository.existsById(1L)).thenReturn(true);

            // When
            goalService.deleteGoal(1L);

            // Then
            verify(goalRepository).existsById(1L);
            verify(goalRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar deletar meta inexistente")
        void shouldThrowExceptionWhenDeletingNonExistentGoal() {
            // Given
            when(goalRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> goalService.deleteGoal(999L))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("Goal não encontrado com ID: 999");
            
            verify(goalRepository).existsById(999L);
            verify(goalRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Update Progress Tests")
    class UpdateProgressTests {

        @Test
        @DisplayName("Deve atualizar progresso com sucesso")
        void shouldUpdateProgressSuccessfully() {
            // Given
            Goal goalWithProgress = Goal.builder()
                    .goalId(goalEntity.getGoalId())
                    .userId(goalEntity.getUserId())
                    .title(goalEntity.getTitle())
                    .description(goalEntity.getDescription())
                    .category(goalEntity.getCategory())
                    .type(goalEntity.getType())
                    .startDate(goalEntity.getStartDate())
                    .endDate(goalEntity.getEndDate())
                    .status(goalEntity.getStatus())
                    .notifications(goalEntity.getNotifications())
                    .createdAt(goalEntity.getCreatedAt())
                    .progress(Progress.builder().completed(5).total(30).build())
                    .build();
            
            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithProgress);
            when(goalMapper.toResponse(goalWithProgress)).thenReturn(goalResponse);

            // When
            GoalResponse result = goalService.updateProgress(1L, progressRequest);

            // Then
            assertThat(result).isNotNull();
            verify(goalRepository).findById(1L);
            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getCompleted() == 6
            ));
            verify(goalMapper).toResponse(goalWithProgress);
        }

        @Test
        @DisplayName("Deve marcar meta como completa quando progresso atingir o total")
        void shouldMarkGoalAsCompletedWhenProgressReachesTotal() {
            // Given
            Goal goalAlmostComplete = Goal.builder()
                    .goalId(goalEntity.getGoalId())
                    .userId(goalEntity.getUserId())
                    .title(goalEntity.getTitle())
                    .description(goalEntity.getDescription())
                    .category(goalEntity.getCategory())
                    .type(goalEntity.getType())
                    .startDate(goalEntity.getStartDate())
                    .endDate(goalEntity.getEndDate())
                    .notifications(goalEntity.getNotifications())
                    .createdAt(goalEntity.getCreatedAt())
                    .progress(Progress.builder().completed(29).total(30).build())
                    .status("active")
                    .build();
            
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
        @DisplayName("Deve lançar exceção ao tentar atualizar progresso de meta inexistente")
        void shouldThrowExceptionWhenUpdatingProgressOfNonExistentGoal() {
            // Given
            when(goalRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> goalService.updateProgress(999L, progressRequest))
                    .isInstanceOf(GoalNotFoundException.class)
                    .hasMessageContaining("Goal não encontrado com ID: 999");
            
            verify(goalRepository).findById(999L);
            verify(goalRepository, never()).save(any());
        }
    }
}