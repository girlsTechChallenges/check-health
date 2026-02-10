package com.fiap.check.health.repository;

import com.fiap.check.health.persistence.entity.Goal;
import com.fiap.check.health.persistence.repository.GoalRepository;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.model.Progress;
import com.fiap.check.health.util.TestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes expandidos para GoalRepository
 * 
 * Cenários avançados incluem:
 * - Consultas por múltiplos critérios
 * - Pesquisas por intervalos de datas
 * - Consultas por categoria e status
 * - Cenários de performance e paginação
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Goal Repository Advanced Tests")
class GoalRepositoryAdvancedTest {

    @Autowired
    private GoalRepository goalRepository;

    private Goal activeGoal;
    private Goal completedGoal;
    private Goal cancelledGoal;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // Goal ativo
        activeGoal = TestDataFactory.GoalEntityBuilder.createValidGoalEntity();
        activeGoal.setUserId("user123");
        activeGoal.setCategory(GoalCategory.SAUDE_FISICA);
        activeGoal.setStatus("active");
        activeGoal.setStartDate(LocalDate.of(2026, 2, 1));
        activeGoal.setEndDate(LocalDate.of(2026, 3, 1));
        activeGoal = goalRepository.save(activeGoal);

        // Goal completado
        completedGoal = TestDataFactory.GoalEntityBuilder.createCompletedGoal();
        completedGoal.setUserId("user123");
        completedGoal.setCategory(GoalCategory.SAUDE_MENTAL);  
        completedGoal.setStatus("completed");
        completedGoal.setStartDate(LocalDate.of(2026, 1, 15));
        completedGoal.setEndDate(LocalDate.of(2026, 2, 15));
        completedGoal = goalRepository.save(completedGoal);

        // Goal cancelado
        cancelledGoal = Goal.builder()
                .userId("user456")
                .title("Meta Cancelada")
                .description("Meta que foi cancelada")
                .category(GoalCategory.NUTRICAO)
                .type("weekly")
                .status("cancelled")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 2, 1))
                .notifications(false)
                .createdAt(LocalDateTime.now())
                .progress(Progress.builder().completed(0).total(10).build())
                .build();
        cancelledGoal = goalRepository.save(cancelledGoal);
    }

    @Nested
    @DisplayName("Find By User And Status Tests")
    class FindByUserAndStatusTests {

        @Test
        @DisplayName("Deve encontrar goals ativos de um usuário")
        void shouldFindActiveGoalsForUser() {
            // When
            List<Goal> activeUserGoals = goalRepository.findByUserIdAndStatus("user123", "active");

            // Then
            assertThat(activeUserGoals).hasSize(1);
            assertThat(activeUserGoals.get(0).getGoalId()).isEqualTo(activeGoal.getGoalId());
            assertThat(activeUserGoals.get(0).getStatus()).isEqualTo("active");
        }

        @Test
        @DisplayName("Deve encontrar goals completados de um usuário")
        void shouldFindCompletedGoalsForUser() {
            // When
            List<Goal> completedUserGoals = goalRepository.findByUserIdAndStatus("user123", "completed");

            // Then
            assertThat(completedUserGoals).hasSize(1);
            assertThat(completedUserGoals.get(0).getGoalId()).isEqualTo(completedGoal.getGoalId());
            assertThat(completedUserGoals.get(0).getStatus()).isEqualTo("completed");
        }

        @Test
        @DisplayName("Deve retornar lista vazia para status inexistente")
        void shouldReturnEmptyListForNonExistentStatus() {
            // When
            List<Goal> nonExistentStatusGoals = goalRepository.findByUserIdAndStatus("user123", "inexistente");

            // Then
            assertThat(nonExistentStatusGoals).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia para usuário inexistente")
        void shouldReturnEmptyListForNonExistentUser() {
            // When
            List<Goal> nonExistentUserGoals = goalRepository.findByUserIdAndStatus("usuarioInexistente", "active");

            // Then
            assertThat(nonExistentUserGoals).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Status Tests")
    class FindByStatusTests {

        @Test
        @DisplayName("Deve encontrar todos os goals ativos")
        void shouldFindAllActiveGoals() {
            // When
            List<Goal> activeGoals = goalRepository.findByStatus("active");

            // Then
            assertThat(activeGoals).hasSize(1);
            assertThat(activeGoals).allMatch(goal -> "active".equals(goal.getStatus()));
        }

        @Test
        @DisplayName("Deve encontrar todos os goals completados")
        void shouldFindAllCompletedGoals() {
            // When
            List<Goal> completedGoals = goalRepository.findByStatus("completed");

            // Then
            assertThat(completedGoals).hasSize(1);
            assertThat(completedGoals).allMatch(goal -> "completed".equals(goal.getStatus()));
        }

        @Test
        @DisplayName("Deve encontrar todos os goals cancelados")
        void shouldFindAllCancelledGoals() {
            // When
            List<Goal> cancelledGoals = goalRepository.findByStatus("cancelled");

            // Then
            assertThat(cancelledGoals).hasSize(1);
            assertThat(cancelledGoals).allMatch(goal -> "cancelled".equals(goal.getStatus()));
        }

        @Test
        @DisplayName("Deve retornar lista vazia para status que não existe")
        void shouldReturnEmptyListForStatusThatDoesNotExist() {
            // When
            List<Goal> inexistentStatusGoals = goalRepository.findByStatus("status_inexistente");

            // Then
            assertThat(inexistentStatusGoals).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Category Tests")
    class FindByCategoryTests {

        @Test
        @DisplayName("Deve encontrar goals por categoria SAUDE_FISICA")
        void shouldFindGoalsBySaudeFisicaCategory() {
            // When
            List<Goal> saudeFisicaGoals = goalRepository.findByCategory(GoalCategory.SAUDE_FISICA);

            // Then
            assertThat(saudeFisicaGoals).hasSize(1);
            assertThat(saudeFisicaGoals.get(0).getCategory()).isEqualTo(GoalCategory.SAUDE_FISICA);
        }

        @Test
        @DisplayName("Deve encontrar goals por categoria SAUDE_MENTAL")
        void shouldFindGoalsBySaudeMentalCategory() {
            // When
            List<Goal> saudeMentalGoals = goalRepository.findByCategory(GoalCategory.SAUDE_MENTAL);

            // Then
            assertThat(saudeMentalGoals).hasSize(1);
            assertThat(saudeMentalGoals.get(0).getCategory()).isEqualTo(GoalCategory.SAUDE_MENTAL);
        }

        @Test
        @DisplayName("Deve encontrar goals por categoria NUTRICAO")
        void shouldFindGoalsByNutricaoCategory() {
            // When
            List<Goal> nutricaoGoals = goalRepository.findByCategory(GoalCategory.NUTRICAO);

            // Then
            assertThat(nutricaoGoals).hasSize(1);
            assertThat(nutricaoGoals.get(0).getCategory()).isEqualTo(GoalCategory.NUTRICAO);
        }

        @Test
        @DisplayName("Deve retornar lista vazia para categoria sem goals")
        void shouldReturnEmptyListForCategoryWithoutGoals() {
            // When
            List<Goal> sonoGoals = goalRepository.findByCategory(GoalCategory.SONO);

            // Then
            assertThat(sonoGoals).isEmpty();
        }

        @Test
        @DisplayName("Deve lidar com multiple goals na mesma categoria")
        void shouldHandleMultipleGoalsInSameCategory() {
            // Given - Adicionar outro goal de SAUDE_FISICA
            Goal anotherPhysicalGoal = Goal.builder()
                    .userId("user789")
                    .title("Outro Goal Físico")
                    .description("Mais um goal de saúde física")
                    .category(GoalCategory.SAUDE_FISICA)
                    .type("daily")
                    .status("active")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(30))
                    .notifications(true)
                    .createdAt(LocalDateTime.now())
                    .progress(Progress.builder().completed(0).total(30).build())
                    .build();
            goalRepository.save(anotherPhysicalGoal);

            // When
            List<Goal> physicalGoals = goalRepository.findByCategory(GoalCategory.SAUDE_FISICA);

            // Then
            assertThat(physicalGoals).hasSize(2);
            assertThat(physicalGoals).allMatch(goal -> 
                GoalCategory.SAUDE_FISICA.equals(goal.getCategory())
            );
        }
    }

    @Nested
    @DisplayName("Find By Date Range Tests")
    class FindByDateRangeTests {

        @Test
        @DisplayName("Deve encontrar goals que iniciam no período")
        void shouldFindGoalsThatStartInPeriod() {
            // Given
            LocalDate searchStart = LocalDate.of(2026, 1, 1);
            LocalDate searchEnd = LocalDate.of(2026, 1, 31);

            // When
            List<Goal> goalsInPeriod = goalRepository.findByStartDateBetween(searchStart, searchEnd);

            // Then
            assertThat(goalsInPeriod).hasSize(2); // completedGoal e cancelledGoal
            assertThat(goalsInPeriod).allMatch(goal -> 
                !goal.getStartDate().isBefore(searchStart) && 
                !goal.getStartDate().isAfter(searchEnd)
            );
        }

        @Test
        @DisplayName("Deve encontrar goals que iniciam exatamente na data de início do período")
        void shouldFindGoalsThatStartExactlyOnPeriodStart() {
            // Given
            LocalDate exactDate = LocalDate.of(2026, 2, 1);
            LocalDate searchEnd = LocalDate.of(2026, 2, 28);

            // When
            List<Goal> exactStartGoals = goalRepository.findByStartDateBetween(exactDate, searchEnd);

            // Then
            assertThat(exactStartGoals).hasSize(1);
            assertThat(exactStartGoals.get(0).getStartDate()).isEqualTo(exactDate);
        }

        @Test
        @DisplayName("Deve retornar lista vazia para período sem goals")
        void shouldReturnEmptyListForPeriodWithoutGoals() {
            // Given
            LocalDate futureStart = LocalDate.of(2027, 1, 1);
            LocalDate futureEnd = LocalDate.of(2027, 12, 31);

            // When
            List<Goal> futureGoals = goalRepository.findByStartDateBetween(futureStart, futureEnd);

            // Then
            assertThat(futureGoals).isEmpty();
        }

        @Test
        @DisplayName("Deve lidar com período de um único dia")
        void shouldHandleSingleDayPeriod() {
            // Given
            LocalDate singleDay = LocalDate.of(2026, 1, 15);

            // When
            List<Goal> singleDayGoals = goalRepository.findByStartDateBetween(singleDay, singleDay);

            // Then
            assertThat(singleDayGoals).hasSize(1);
            assertThat(singleDayGoals.get(0).getStartDate()).isEqualTo(singleDay);
        }
    }

    @Nested
    @DisplayName("Performance And Edge Cases Tests")
    class PerformanceAndEdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com múltiplos usuários eficientemente")
        void shouldHandleMultipleUsersEfficiently() {
            // Given - Criar goals para múltiplos usuários
            for (int i = 1; i <= 10; i++) {
                Goal goal = Goal.builder()
                        .userId("user" + i)
                        .title("Goal " + i)
                        .description("Descrição " + i)
                        .category(GoalCategory.SAUDE_FISICA)
                        .type("daily")
                        .status("active")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .notifications(true)
                        .createdAt(LocalDateTime.now())
                        .progress(Progress.builder().completed(0).total(30).build())
                        .build();
                goalRepository.save(goal);
            }

            // When
            List<Goal> user5Goals = goalRepository.findByUserId("user5");

            // Then
            assertThat(user5Goals).hasSize(1);
            assertThat(user5Goals.get(0).getUserId()).isEqualTo("user5");
        }

        @Test
        @DisplayName("Deve verificar se repository funciona corretamente após saveAndFlush")
        void shouldVerifyRepositoryWorksCorrectlyAfterSaveAndFlush() {
            // Given
            Goal newGoal = Goal.builder()
                    .userId("flushUser")
                    .title("Goal para Flush")
                    .description("Teste de flush")
                    .category(GoalCategory.BEM_ESTAR)
                    .type("weekly")
                    .status("active")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusWeeks(4))
                    .notifications(true)
                    .createdAt(LocalDateTime.now())
                    .progress(Progress.builder().completed(0).total(4).build())
                    .build();

            // When
            Goal savedGoal = goalRepository.saveAndFlush(newGoal);

            // Then
            assertThat(savedGoal.getGoalId()).isNotNull();

            Optional<Goal> retrievedGoal = goalRepository.findById(savedGoal.getGoalId());
            assertThat(retrievedGoal).isPresent();
            assertThat(retrievedGoal.get().getTitle()).isEqualTo("Goal para Flush");
        }

        @Test
        @DisplayName("Deve lidar com consultas quando repository está vazio")
        void shouldHandleQueriesWhenRepositoryIsEmpty() {
            // Given
            goalRepository.deleteAll();

            // When & Then - Todas as consultas devem retornar listas vazias
            assertThat(goalRepository.findAll()).isEmpty();
            assertThat(goalRepository.findByUserId("anyUser")).isEmpty();
            assertThat(goalRepository.findByStatus("anyStatus")).isEmpty();
            assertThat(goalRepository.findByCategory(GoalCategory.SONO)).isEmpty();
            assertThat(goalRepository.findByUserIdAndStatus("anyUser", "anyStatus")).isEmpty();
            
            LocalDate today = LocalDate.now();
            assertThat(goalRepository.findByStartDateBetween(today, today)).isEmpty();
        }

        @Test
        @DisplayName("Deve verificar contagem de goals por método count")
        void shouldVerifyGoalCountByCountMethod() {
            // When
            long totalGoals = goalRepository.count();

            // Then
            assertThat(totalGoals).isEqualTo(3L); // activeGoal + completedGoal + cancelledGoal
        }

        @Test
        @DisplayName("Deve verificar existência de goal por ID usando existsById")
        void shouldVerifyGoalExistenceByIdUsingExistsById() {
            // When & Then
            assertThat(goalRepository.existsById(activeGoal.getGoalId())).isTrue();
            assertThat(goalRepository.existsById(999L)).isFalse();
            assertThat(goalRepository.existsById(-1L)).isFalse();
            assertThat(goalRepository.existsById(0L)).isFalse();
        }
    }
}