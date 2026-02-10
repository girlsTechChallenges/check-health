package com.fiap.check.health.api.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para o modelo GoalResponse (API)
 */
@DisplayName("GoalResponse Model Tests")
class GoalResponseTest {

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorAndBuilderTests {

        @Test
        @DisplayName("Deve criar GoalResponse usando o builder")
        void shouldCreateGoalResponseUsingBuilder() {
            // Given
            OffsetDateTime now = OffsetDateTime.now();
            GoalResponseProgress progress = GoalResponseProgress.builder()
                    .completed(15)
                    .total(30)
                    .unit("days")
                    .build();

            GoalResponseGamification gamification = GoalResponseGamification.builder()
                    .pointsEarned(150)
                    .badge("beginner")
                    .userLevel(2)
                    .build();

            // When
            GoalResponse goalResponse = GoalResponse.builder()
                    .goalId("123")
                    .userId("user456")
                    .title("Meta de Exercícios")
                    .status("active")
                    .createdAt(now)
                    .progress(progress)
                    .gamification(gamification)
                    .message("Progresso: 15/30 dias")
                    .build();

            // Then
            assertThat(goalResponse.getGoalId()).isEqualTo("123");
            assertThat(goalResponse.getUserId()).isEqualTo("user456");
            assertThat(goalResponse.getTitle()).isEqualTo("Meta de Exercícios");
            assertThat(goalResponse.getStatus()).isEqualTo("active");
            assertThat(goalResponse.getCreatedAt()).isEqualTo(now);
            assertThat(goalResponse.getProgress()).isEqualTo(progress);
            assertThat(goalResponse.getGamification()).isEqualTo(gamification);
            assertThat(goalResponse.getMessage()).isEqualTo("Progresso: 15/30 dias");
        }

        @Test
        @DisplayName("Deve criar GoalResponse sem campos opcionais")
        void shouldCreateGoalResponseWithoutOptionalFields() {
            // When
            GoalResponse goalResponse = GoalResponse.builder()
                    .goalId("123")
                    .userId("user456")
                    .title("Meta Simples")
                    .status("active")
                    .build();

            // Then
            assertThat(goalResponse.getGoalId()).isEqualTo("123");
            assertThat(goalResponse.getUserId()).isEqualTo("user456");
            assertThat(goalResponse.getTitle()).isEqualTo("Meta Simples");
            assertThat(goalResponse.getStatus()).isEqualTo("active");
            assertThat(goalResponse.getCreatedAt()).isNull();
            assertThat(goalResponse.getProgress()).isNull();
            assertThat(goalResponse.getGamification()).isNull();
            assertThat(goalResponse.getMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("Equality and Hash Code Tests")
    class EqualityAndHashCodeTests {

        @Test
        @DisplayName("Deve ter equals e hashCode funcionais")
        void shouldHaveWorkingEqualsAndHashCode() {
            // Given
            GoalResponse response1 = GoalResponse.builder()
                    .goalId("123")
                    .userId("user456")
                    .title("Meta Teste")
                    .status("active")
                    .build();

            GoalResponse response2 = GoalResponse.builder()
                    .goalId("123")
                    .userId("user456")
                    .title("Meta Teste")
                    .status("active")
                    .build();

            GoalResponse response3 = GoalResponse.builder()
                    .goalId("456")
                    .userId("user456")
                    .title("Meta Teste")
                    .status("active")
                    .build();

            // When / Then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
            assertThat(response1).isNotEqualTo(response3);
            assertThat(response1.hashCode()).isNotEqualTo(response3.hashCode());
        }

        @Test
        @DisplayName("Deve ter toString funcional")
        void shouldHaveWorkingToString() {
            // Given
            GoalResponse goalResponse = GoalResponse.builder()
                    .goalId("123")
                    .userId("user456")
                    .title("Meta de Exercícios")
                    .status("active")
                    .build();

            // When
            String toString = goalResponse.toString();

            // Then
            assertThat(toString).contains("123");
            assertThat(toString).contains("user456");
            assertThat(toString).contains("Meta de Exercícios");
            assertThat(toString).contains("active");
        }
    }
}

/**
 * Testes para o modelo GoalResponseProgress (API)
 */
@DisplayName("GoalResponseProgress Model Tests")
class GoalResponseProgressTest {

    @Test
    @DisplayName("Deve criar GoalResponseProgress usando builder")
    void shouldCreateGoalResponseProgressUsingBuilder() {
        // When
        GoalResponseProgress progress = GoalResponseProgress.builder()
                .completed(25)
                .total(100)
                .unit("points")
                .build();

        // Then
        assertThat(progress.getCompleted()).isEqualTo(25);
        assertThat(progress.getTotal()).isEqualTo(100);
        assertThat(progress.getUnit()).isEqualTo("points");
    }

    @Test
    @DisplayName("Deve ter equals e hashCode funcionais")
    void shouldHaveWorkingEqualsAndHashCode() {
        // Given
        GoalResponseProgress progress1 = GoalResponseProgress.builder()
                .completed(25)
                .total(100)
                .unit("points")
                .build();

        GoalResponseProgress progress2 = GoalResponseProgress.builder()
                .completed(25)
                .total(100)
                .unit("points")
                .build();

        // When / Then
        assertThat(progress1).isEqualTo(progress2);
        assertThat(progress1.hashCode()).isEqualTo(progress2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString funcional")
    void shouldHaveWorkingToString() {
        // Given
        GoalResponseProgress progress = GoalResponseProgress.builder()
                .completed(50)
                .total(100)
                .unit("days")
                .build();

        // When
        String toString = progress.toString();

        // Then
        assertThat(toString).contains("50");
        assertThat(toString).contains("100");
        assertThat(toString).contains("days");
    }
}

/**
 * Testes para o modelo GoalResponseGamification (API)
 */
@DisplayName("GoalResponseGamification Model Tests")
class GoalResponseGamificationTest {

    @Test
    @DisplayName("Deve criar GoalResponseGamification usando builder")
    void shouldCreateGoalResponseGamificationUsingBuilder() {
        // When
        GoalResponseGamification gamification = GoalResponseGamification.builder()
                .pointsEarned(500)
                .badge("Expert")
                .userLevel(5)
                .build();

        // Then
        assertThat(gamification.getPointsEarned()).isEqualTo(500);
        assertThat(gamification.getBadge()).isEqualTo("Expert");
        assertThat(gamification.getUserLevel()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve criar GoalResponseGamification com valores null")
    void shouldCreateGoalResponseGamificationWithNullValues() {
        // When
        GoalResponseGamification gamification = GoalResponseGamification.builder()
                .pointsEarned(null)
                .badge(null)
                .userLevel(1)
                .build();

        // Then
        assertThat(gamification.getPointsEarned()).isNull();
        assertThat(gamification.getBadge()).isNull();
        assertThat(gamification.getUserLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve ter equals e hashCode funcionais")
    void shouldHaveWorkingEqualsAndHashCode() {
        // Given
        GoalResponseGamification gamification1 = GoalResponseGamification.builder()
                .pointsEarned(500)
                .badge("Expert")
                .userLevel(5)
                .build();

        GoalResponseGamification gamification2 = GoalResponseGamification.builder()
                .pointsEarned(500)
                .badge("Expert")
                .userLevel(5)
                .build();

        // When / Then
        assertThat(gamification1).isEqualTo(gamification2);
        assertThat(gamification1.hashCode()).isEqualTo(gamification2.hashCode());
    }
}

/**
 * Testes para o modelo ProgressRequest (API)
 */
@DisplayName("ProgressRequest Model Tests")
class ProgressRequestTest {

    @Test
    @DisplayName("Deve criar ProgressRequest usando builder")
    void shouldCreateProgressRequestUsingBuilder() {
        // When
        ProgressRequest progressRequest = ProgressRequest.builder()
                .increment(5)
                .unit("steps")
                .build();

        // Then
        assertThat(progressRequest.getIncrement()).isEqualTo(5);
        assertThat(progressRequest.getUnit()).isEqualTo("steps");
    }

    @Test
    @DisplayName("Deve criar ProgressRequest com valores mínimos")
    void shouldCreateProgressRequestWithMinimalValues() {
        // When
        ProgressRequest progressRequest = ProgressRequest.builder()
                .increment(1)
                .build();

        // Then
        assertThat(progressRequest.getIncrement()).isEqualTo(1);
        assertThat(progressRequest.getUnit()).isNull();
    }

    @Test
    @DisplayName("Deve ter equals e hashCode funcionais")
    void shouldHaveWorkingEqualsAndHashCode() {
        // Given
        ProgressRequest request1 = ProgressRequest.builder()
                .increment(10)
                .unit("kilometers")
                .build();

        ProgressRequest request2 = ProgressRequest.builder()
                .increment(10)
                .unit("kilometers")
                .build();

        // When / Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString funcional")
    void shouldHaveWorkingToString() {
        // Given
        ProgressRequest progressRequest = ProgressRequest.builder()
                .increment(3)
                .unit("workouts")
                .build();

        // When
        String toString = progressRequest.toString();

        // Then
        assertThat(toString).contains("3");
        assertThat(toString).contains("workouts");
    }
}