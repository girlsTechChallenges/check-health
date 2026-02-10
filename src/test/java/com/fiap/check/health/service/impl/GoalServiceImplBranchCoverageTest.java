package com.fiap.check.health.service.impl;

import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.api.model.ProgressRequest;
import com.fiap.check.health.event.publisher.GoalEventPublisher;
import com.fiap.check.health.mapper.GoalMapper;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes específicos para cobertura de Branches (Condicionais e Switch Cases)
 * que atualmente têm 0% de cobertura no GoalServiceImpl.
 * Foca em testar todos os caminhos condicionais dos métodos privados e públicos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoalServiceImpl - Branch Coverage Tests")
class GoalServiceImplBranchCoverageTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private GoalEventPublisher goalEventPublisher;

    @InjectMocks
    private GoalServiceImpl goalService;

    private GoalRequest goalRequest;
    private GoalResponse goalResponse;
    private Goal goalEntity;

    @BeforeEach
    void setUp() {
        goalRequest = TestDataFactory.GoalRequestBuilder.createValidGoalRequest();
        goalResponse = TestDataFactory.GoalResponseBuilder.createValidGoalResponse();
        goalEntity = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
    }

    @Nested
    @DisplayName("Testes para calculateDefaultTotal() - Switch Cases")
    class CalculateDefaultTotalBranchTests {

        @Test
        @DisplayName("Deve calcular total correto para tipo 'daily' com datas definidas")
        void shouldCalculateCorrectTotalForDailyWithDates() {
            // Given
            Goal dailyGoal = Goal.builder()
                    .type("daily")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(9)) // 10 days total
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(dailyGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(dailyGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(10); // 9 days difference + 1
                assertThat(goal.getProgress().getUnit()).isEqualTo("days");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve calcular total correto para tipo 'weekly' com datas definidas")
        void shouldCalculateCorrectTotalForWeeklyWithDates() {
            // Given
            Goal weeklyGoal = Goal.builder()
                    .type("weekly")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusWeeks(3)) // 21 days = 3 weeks
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(weeklyGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(weeklyGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(4); // (21 / 7) + 1
                assertThat(goal.getProgress().getUnit()).isEqualTo("weeks");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve calcular total correto para tipo 'monthly' com datas definidas")
        void shouldCalculateCorrectTotalForMonthlyWithDates() {
            // Given
            Goal monthlyGoal = Goal.builder()
                    .type("monthly")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(90)) // ~3 months
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(monthlyGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(monthlyGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(4); // (90 / 30) + 1
                assertThat(goal.getProgress().getUnit()).isEqualTo("months");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve calcular total correto para tipo 'single' com datas definidas")
        void shouldCalculateCorrectTotalForSingleWithDates() {
            // Given
            Goal singleGoal = Goal.builder()
                    .type("single")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(100))
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(singleGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(singleGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(1);
                assertThat(goal.getProgress().getUnit()).isEqualTo("goal");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve usar valor padrão para tipo desconhecido com datas definidas")
        void shouldUseDefaultForUnknownTypeWithDates() {
            // Given
            Goal unknownTypeGoal = Goal.builder()
                    .type("custom_unknown_type")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(50))
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(unknownTypeGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(unknownTypeGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(30); // default value
                assertThat(goal.getProgress().getUnit()).isEqualTo("days");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve usar valor padrão para tipo null com datas definidas")
        void shouldUseDefaultForNullTypeWithDates() {
            // Given
            Goal nullTypeGoal = Goal.builder()
                    .type(null)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(20))
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(nullTypeGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(nullTypeGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(21); // 20 days difference + 1 (treated as daily)
                assertThat(goal.getProgress().getUnit()).isEqualTo("days");
                return true;
            }));
        }
    }

    @Nested
    @DisplayName("Testes para calculateDefaultTotal() sem datas - Fallback Switch")
    class CalculateDefaultTotalFallbackBranchTests {

        @Test
        @DisplayName("Deve usar valores padrão para 'daily' sem datas definidas")
        void shouldUseDefaultForDailyWithoutDates() {
            // Given
            Goal goalWithoutDates = Goal.builder()
                    .type("daily")
                    .startDate(null)
                    .endDate(null)
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(goalWithoutDates);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithoutDates);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(30); // default for daily
                assertThat(goal.getProgress().getUnit()).isEqualTo("days");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve usar valores padrão para 'weekly' sem datas definidas")
        void shouldUseDefaultForWeeklyWithoutDates() {
            // Given
            Goal goalWithoutDates = Goal.builder()
                    .type("weekly")
                    .startDate(null)
                    .endDate(null)
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(goalWithoutDates);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithoutDates);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(4); // default for weekly
                assertThat(goal.getProgress().getUnit()).isEqualTo("weeks");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve usar valores padrão para 'monthly' sem datas definidas")
        void shouldUseDefaultForMonthlyWithoutDates() {
            // Given
            Goal goalWithoutDates = Goal.builder()
                    .type("monthly")
                    .startDate(null)
                    .endDate(null)
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(goalWithoutDates);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithoutDates);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(1); // default for monthly
                assertThat(goal.getProgress().getUnit()).isEqualTo("months");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve usar valores padrão para 'single' sem datas definidas")
        void shouldUseDefaultForSingleWithoutDates() {
            // Given
            Goal goalWithoutDates = Goal.builder()
                    .type("single")
                    .startDate(null)
                    .endDate(null)
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(goalWithoutDates);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithoutDates);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(1); // default for single
                assertThat(goal.getProgress().getUnit()).isEqualTo("goal");
                return true;
            }));
        }

        @Test
        @DisplayName("Deve usar valores padrão para tipo desconhecido sem datas definidas")
        void shouldUseDefaultForUnknownTypeWithoutDates() {
            // Given
            Goal goalWithoutDates = Goal.builder()
                    .type("unknown_type")
                    .startDate(null)
                    .endDate(null)
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(goalWithoutDates);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithoutDates);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getTotal()).isEqualTo(30); // default fallback
                assertThat(goal.getProgress().getUnit()).isEqualTo("days");
                return true;
            }));
        }
    }

    @Nested
    @DisplayName("Testes para getDefaultUnit() - Switch Cases")
    class GetDefaultUnitBranchTests {

        @Test
        @DisplayName("Deve retornar 'days' para tipo daily")
        void shouldReturnDaysForDailyType() {
            // Teste implícito através do createGoal já testado acima
            // mas vamos criar um explícito para garantir
            Goal dailyGoal = Goal.builder()
                    .type("daily")
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(dailyGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(dailyGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            goalService.createGoal(goalRequest);

            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getUnit().equals("days")));
        }

        @Test
        @DisplayName("Deve retornar 'weeks' para tipo weekly")
        void shouldReturnWeeksForWeeklyType() {
            Goal weeklyGoal = Goal.builder()
                    .type("weekly")
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(weeklyGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(weeklyGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            goalService.createGoal(goalRequest);

            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getUnit().equals("weeks")));
        }

        @Test
        @DisplayName("Deve retornar 'months' para tipo monthly")
        void shouldReturnMonthsForMonthlyType() {
            Goal monthlyGoal = Goal.builder()
                    .type("monthly")
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(monthlyGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(monthlyGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            goalService.createGoal(goalRequest);

            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getUnit().equals("months")));
        }

        @Test
        @DisplayName("Deve retornar 'goal' para tipo single")
        void shouldReturnGoalForSingleType() {
            Goal singleGoal = Goal.builder()
                    .type("single")
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(singleGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(singleGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            goalService.createGoal(goalRequest);

            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getUnit().equals("goal")));
        }

        @Test
        @DisplayName("Deve retornar 'days' para tipo null (default)")
        void shouldReturnDaysForNullType() {
            Goal nullTypeGoal = Goal.builder()
                    .type(null)
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(nullTypeGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(nullTypeGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            goalService.createGoal(goalRequest);

            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getUnit().equals("days")));
        }

        @Test
        @DisplayName("Deve retornar 'days' para tipo desconhecido (default)")
        void shouldReturnDaysForUnknownType() {
            Goal unknownTypeGoal = Goal.builder()
                    .type("unknown_type")
                    .progress(null)
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(unknownTypeGoal);
            when(goalRepository.save(any(Goal.class))).thenReturn(unknownTypeGoal);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            goalService.createGoal(goalRequest);

            verify(goalRepository).save(argThat(goal -> 
                goal.getProgress().getUnit().equals("days")));
        }
    }

    @Nested
    @DisplayName("Testes para updateProgress() - Branches condicionais")
    class UpdateProgressBranchTests {

        @Test
        @DisplayName("Deve atualizar progresso quando goal.progress não é null")
        void shouldUpdateProgressWhenGoalProgressIsNotNull() {
            // Given
            Goal goalWithProgress = Goal.builder()
                    .goalId(1L)
                    .status("active")
                    .progress(Progress.builder().completed(10).total(30).build())
                    .build();

            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(5)
                    .build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithProgress);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, progressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress().getCompleted()).isEqualTo(15); // 10 + 5
                assertThat(goal.getStatus()).isEqualTo("active"); // não deve completar ainda
                return true;
            }));
        }

        @Test
        @DisplayName("Deve marcar goal como completed quando progresso atinge total")
        void shouldMarkAsCompletedWhenProgressReachesTotal() {
            // Given
            Goal goalWithProgress = Goal.builder()
                    .goalId(1L)
                    .status("active") 
                    .progress(Progress.builder().completed(29).total(30).build())
                    .build();

            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(1)
                    .build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithProgress);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, progressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress().getCompleted()).isEqualTo(30); // 29 + 1
                assertThat(goal.getStatus()).isEqualTo("completed"); // deve completar
                return true;
            }));
        }

        @Test
        @DisplayName("Deve marcar goal como completed quando progresso excede total")
        void shouldMarkAsCompletedWhenProgressExceedsTotal() {
            // Given
            Goal goalWithProgress = Goal.builder()
                    .goalId(1L)
                    .status("active")
                    .progress(Progress.builder().completed(25).total(30).build())
                    .build();

            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(10)
                    .build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithProgress);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, progressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress().getCompleted()).isEqualTo(35); // 25 + 10
                assertThat(goal.getStatus()).isEqualTo("completed"); // deve completar
                return true;
            }));
        }

        @Test
        @DisplayName("Não deve atualizar progresso quando goal.progress é null")
        void shouldNotUpdateProgressWhenGoalProgressIsNull() {
            // Given
            Goal goalWithoutProgress = Goal.builder()
                    .goalId(1L)
                    .status("active")
                    .progress(null) // Progress é null
                    .build();

            ProgressRequest progressRequest = ProgressRequest.builder()
                    .increment(5)
                    .build();

            when(goalRepository.findById(1L)).thenReturn(Optional.of(goalWithoutProgress));
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithoutProgress);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.updateProgress(1L, progressRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNull(); // Progress deve continuar null
                assertThat(goal.getStatus()).isEqualTo("active"); // Status não muda
                return true;
            }));
        }
    }

    @Nested
    @DisplayName("Testes para createGoal - Branch de progress null vs not null")
    class CreateGoalProgressBranchTests {

        @Test
        @DisplayName("Deve manter progresso existente quando goal já tem progress")
        void shouldKeepExistingProgressWhenGoalAlreadyHasProgress() {
            // Given
            Goal goalWithExistingProgress = Goal.builder()
                    .userId("user123")
                    .title("Meta com progresso")
                    .progress(Progress.builder().completed(5).total(10).unit("custom").build())
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(goalWithExistingProgress);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithExistingProgress);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getCompleted()).isEqualTo(5); // mantém existing
                assertThat(goal.getProgress().getTotal()).isEqualTo(10); // mantém existing
                assertThat(goal.getProgress().getUnit()).isEqualTo("custom"); // mantém existing
                return true;
            }));
        }

        @Test
        @DisplayName("Deve inicializar progresso padrão quando goal.progress é null")
        void shouldInitializeDefaultProgressWhenGoalProgressIsNull() {
            // Given
            Goal goalWithoutProgress = Goal.builder()
                    .userId("user123")
                    .title("Meta sem progresso")
                    .type("daily")
                    .progress(null) // Explicitamente null
                    .build();

            when(goalMapper.toEntity(any(GoalRequest.class))).thenReturn(goalWithoutProgress);
            when(goalRepository.save(any(Goal.class))).thenReturn(goalWithoutProgress);
            when(goalMapper.toResponse(any(Goal.class))).thenReturn(goalResponse);

            // When
            goalService.createGoal(goalRequest);

            // Then
            verify(goalRepository).save(argThat(goal -> {
                assertThat(goal.getProgress()).isNotNull();
                assertThat(goal.getProgress().getCompleted()).isEqualTo(0); // inicializado
                assertThat(goal.getProgress().getTotal()).isEqualTo(30); // default for daily
                assertThat(goal.getProgress().getUnit()).isEqualTo("days"); // default for daily
                return true;
            }));
        }
    }
}