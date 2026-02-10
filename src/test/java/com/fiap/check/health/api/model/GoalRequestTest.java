package com.fiap.check.health.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para o modelo GoalRequest (API)
 * Garante que os modelos gerados estão funcionando corretamente
 */
@DisplayName("GoalRequest Model Tests")
class GoalRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorAndBuilderTests {

        @Test
        @DisplayName("Deve criar GoalRequest usando o builder")
        void shouldCreateGoalRequestUsingBuilder() {
            // Given / When
            GoalRequest goalRequest = GoalRequest.builder()
                    .userId("user123")
                    .title("Exercitar diariamente")
                    .description("Meta de 30 minutos de exercícios por dia")
                    .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                    .type(GoalRequest.TypeEnum.DAILY)
                    .startDate(LocalDate.of(2026, 2, 9))
                    .endDate(LocalDate.of(2026, 3, 11))
                    .notifications(true)
                    .build();

            // Then
            assertThat(goalRequest.getUserId()).isEqualTo("user123");
            assertThat(goalRequest.getTitle()).isEqualTo("Exercitar diariamente");
            assertThat(goalRequest.getDescription()).isEqualTo("Meta de 30 minutos de exercícios por dia");
            assertThat(goalRequest.getCategory()).isEqualTo(GoalRequest.CategoryEnum.SAUDE_FISICA);
            assertThat(goalRequest.getType()).isEqualTo(GoalRequest.TypeEnum.DAILY);
            assertThat(goalRequest.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 9));
            assertThat(goalRequest.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 11));
            assertThat(goalRequest.getNotifications()).isTrue();
        }

        @Test
        @DisplayName("Deve criar GoalRequest com frequency")
        void shouldCreateGoalRequestWithFrequency() {
            // Given
            GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                    .periodicity("daily")
                    .timesPerPeriod(1)
                    .build();

            // When
            GoalRequest goalRequest = GoalRequest.builder()
                    .userId("user123")
                    .title("Meta com frequência")
                    .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                    .type(GoalRequest.TypeEnum.DAILY)
                    .startDate(LocalDate.now())
                    .frequency(frequency)
                    .build();

            // Then
            assertThat(goalRequest.getFrequency()).isNotNull();
            assertThat(goalRequest.getFrequency().getPeriodicity()).isEqualTo("daily");
            assertThat(goalRequest.getFrequency().getTimesPerPeriod()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve criar GoalRequest com reward")
        void shouldCreateGoalRequestWithReward() {
            // Given
            GoalRequestReward reward = GoalRequestReward.builder()
                    .points(50)
                    .badge("Health Warrior")
                    .build();

            // When
            GoalRequest goalRequest = GoalRequest.builder()
                    .userId("user123")
                    .title("Meta com recompensa")
                    .category(GoalRequest.CategoryEnum.BEM_ESTAR)
                    .type(GoalRequest.TypeEnum.SINGLE)
                    .startDate(LocalDate.now())
                    .reward(reward)
                    .build();

            // Then
            assertThat(goalRequest.getReward()).isNotNull();
            assertThat(goalRequest.getReward().getPoints()).isEqualTo(50);
            assertThat(goalRequest.getReward().getBadge()).isEqualTo("Health Warrior");
        }
    }

    @Nested
    @DisplayName("Category Enum Tests")
    class CategoryEnumTests {

        @Test
        @DisplayName("Deve validar todos os valores do enum Category")
        void shouldValidateAllCategoryEnumValues() {
            // When / Then
            assertThat(GoalRequest.CategoryEnum.SAUDE_FISICA.getValue()).isEqualTo("SAUDE_FISICA");
            assertThat(GoalRequest.CategoryEnum.SAUDE_MENTAL.getValue()).isEqualTo("SAUDE_MENTAL");
            assertThat(GoalRequest.CategoryEnum.NUTRICAO.getValue()).isEqualTo("NUTRICAO");
            assertThat(GoalRequest.CategoryEnum.SONO.getValue()).isEqualTo("SONO");
            assertThat(GoalRequest.CategoryEnum.BEM_ESTAR.getValue()).isEqualTo("BEM_ESTAR");
        }

        @Test
        @DisplayName("Deve converter string para CategoryEnum")
        void shouldConvertStringToCategoryEnum() {
            // When / Then
            assertThat(GoalRequest.CategoryEnum.fromValue("SAUDE_FISICA"))
                    .isEqualTo(GoalRequest.CategoryEnum.SAUDE_FISICA);
            assertThat(GoalRequest.CategoryEnum.fromValue("SAUDE_MENTAL"))
                    .isEqualTo(GoalRequest.CategoryEnum.SAUDE_MENTAL);
            assertThat(GoalRequest.CategoryEnum.fromValue("NUTRICAO"))
                    .isEqualTo(GoalRequest.CategoryEnum.NUTRICAO);
            assertThat(GoalRequest.CategoryEnum.fromValue("SONO"))
                    .isEqualTo(GoalRequest.CategoryEnum.SONO);
            assertThat(GoalRequest.CategoryEnum.fromValue("BEM_ESTAR"))
                    .isEqualTo(GoalRequest.CategoryEnum.BEM_ESTAR);
        }

        @Test
        @DisplayName("Deve lançar exceção para valor de categoria inválido")
        void shouldThrowExceptionForInvalidCategoryValue() {
            // When / Then
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                    GoalRequest.CategoryEnum.fromValue("CATEGORIA_INVALIDA")
            );
        }
    }

    @Nested
    @DisplayName("Type Enum Tests")
    class TypeEnumTests {

        @Test
        @DisplayName("Deve validar todos os valores do enum Type")
        void shouldValidateAllTypeEnumValues() {
            // When / Then
            assertThat(GoalRequest.TypeEnum.DAILY.getValue()).isEqualTo("daily");
            assertThat(GoalRequest.TypeEnum.WEEKLY.getValue()).isEqualTo("weekly");
            assertThat(GoalRequest.TypeEnum.MONTHLY.getValue()).isEqualTo("monthly");
            assertThat(GoalRequest.TypeEnum.SINGLE.getValue()).isEqualTo("single");
        }

        @Test
        @DisplayName("Deve converter string para TypeEnum")
        void shouldConvertStringToTypeEnum() {
            // When / Then
            assertThat(GoalRequest.TypeEnum.fromValue("daily"))
                    .isEqualTo(GoalRequest.TypeEnum.DAILY);
            assertThat(GoalRequest.TypeEnum.fromValue("weekly"))
                    .isEqualTo(GoalRequest.TypeEnum.WEEKLY);
            assertThat(GoalRequest.TypeEnum.fromValue("monthly"))
                    .isEqualTo(GoalRequest.TypeEnum.MONTHLY);
            assertThat(GoalRequest.TypeEnum.fromValue("single"))
                    .isEqualTo(GoalRequest.TypeEnum.SINGLE);
        }

        @Test
        @DisplayName("Deve lançar exceção para valor de tipo inválido")
        void shouldThrowExceptionForInvalidTypeValue() {
            // When / Then
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                    GoalRequest.TypeEnum.fromValue("tipo_invalido")
            );
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Deve ter métodos toString funcionais")
        void shouldHaveWorkingToStringMethods() {
            // Given
            GoalRequest goalRequest = GoalRequest.builder()
                    .userId("user123")
                    .title("Test Goal")
                    .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                    .type(GoalRequest.TypeEnum.DAILY)
                    .startDate(LocalDate.of(2026, 2, 9))
                    .notifications(true)
                    .build();

            // When
            String toString = goalRequest.toString();

            // Then
            assertThat(toString).contains("user123");
            assertThat(toString).contains("Test Goal");
            assertThat(toString).contains("SAUDE_FISICA");
            assertThat(toString).contains("daily");
        }

        @Test
        @DisplayName("Deve testar equals e hashCode")
        void shouldTestEqualsAndHashCode() {
            // Given
            GoalRequest goalRequest1 = GoalRequest.builder()
                    .userId("user123")
                    .title("Test Goal")
                    .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                    .type(GoalRequest.TypeEnum.DAILY)
                    .startDate(LocalDate.of(2026, 2, 9))
                    .build();

            GoalRequest goalRequest2 = GoalRequest.builder()
                    .userId("user123")
                    .title("Test Goal")
                    .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                    .type(GoalRequest.TypeEnum.DAILY)
                    .startDate(LocalDate.of(2026, 2, 9))
                    .build();

            // When / Then
            assertThat(goalRequest1).isEqualTo(goalRequest2);
            assertThat(goalRequest1.hashCode()).isEqualTo(goalRequest2.hashCode());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Deve criar GoalRequest com valores mínimos obrigatórios")
        void shouldCreateGoalRequestWithRequiredMinimumValues() {
            // Given / When
            GoalRequest goalRequest = GoalRequest.builder()
                    .userId("user123")
                    .title("Required Goal")
                    .category(GoalRequest.CategoryEnum.SAUDE_FISICA)
                    .type(GoalRequest.TypeEnum.DAILY)
                    .startDate(LocalDate.now())
                    .build();

            // Then
            assertThat(goalRequest.getUserId()).isNotNull();
            assertThat(goalRequest.getTitle()).isNotNull();
            assertThat(goalRequest.getCategory()).isNotNull();
            assertThat(goalRequest.getType()).isNotNull();
            assertThat(goalRequest.getStartDate()).isNotNull();
        }
    }
}