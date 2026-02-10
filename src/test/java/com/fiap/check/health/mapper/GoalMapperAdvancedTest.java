package com.fiap.check.health.mapper;

import com.fiap.check.health.api.model.*;
import com.fiap.check.health.model.*;
import com.fiap.check.health.persistence.entity.Goal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes expandidos para GoalMapper
 * 
 * Cenários adicionais incluem:
 * - Conversões com todos os campos preenchidos
 * - Mapeamento de enums e objetos complexos
 * - Validação de mensagens de progresso
 * - Casos extremos e edge cases
 */
@DisplayName("GoalMapper Advanced Tests")
class GoalMapperAdvancedTest {

    private GoalMapper goalMapper;

    @BeforeEach
    void setUp() {
        goalMapper = new GoalMapper();
    }

    @Nested
    @DisplayName("Complete Field Mapping Tests")
    class CompleteFieldMappingTests {

        @Test
        @DisplayName("Deve mapear todos os campos de GoalRequest para Goal entity")
        void shouldMapAllFieldsFromGoalRequestToEntity() {
            // Given
            GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                    .timesPerPeriod(3)
                    .periodicity("weekly")
                    .build();

            GoalRequestReward reward = GoalRequestReward.builder()
                    .points(50)
                    .badge("Super Achiever")
                    .build();

            GoalRequest request = GoalRequest.builder()
                    .userId("advancedUser")
                    .title("Meta Avançada")
                    .description("Descrição detalhada da meta avançada")
                    .category(GoalRequest.CategoryEnum.SAUDE_MENTAL)
                    .type(GoalRequest.TypeEnum.WEEKLY)
                    .startDate(LocalDate.of(2026, 3, 1))
                    .endDate(LocalDate.of(2026, 6, 1))
                    .frequency(frequency)
                    .difficulty(GoalRequest.DifficultyEnum.HARD)
                    .reward(reward)
                    .status(GoalRequest.StatusEnum.ACTIVE)
                    .notifications(true)
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo("advancedUser");
            assertThat(result.getTitle()).isEqualTo("Meta Avançada");
            assertThat(result.getDescription()).isEqualTo("Descrição detalhada da meta avançada");
            assertThat(result.getCategory()).isEqualTo(GoalCategory.SAUDE_MENTAL);
            assertThat(result.getType()).isEqualTo("weekly");
            assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 1));
            assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(result.getDifficulty()).isEqualTo("hard");
            assertThat(result.getStatus()).isEqualTo("active");
            assertThat(result.getNotifications()).isTrue();

            // Frequency mapping
            assertThat(result.getFrequency()).isNotNull();
            assertThat(result.getFrequency().getTimesPerPeriod()).isEqualTo(3);
            assertThat(result.getFrequency().getPeriodicity()).isEqualTo("weekly");

            // Reward mapping
            assertThat(result.getReward()).isNotNull();
            assertThat(result.getReward().getPoints()).isEqualTo(50);
            assertThat(result.getReward().getBadge()).isEqualTo("Super Achiever");
        }

        @Test
        @DisplayName("Deve mapear todos os campos de Goal entity para GoalResponse")
        void shouldMapAllFieldsFromEntityToGoalResponse() {
            // Given
            Frequency frequency = Frequency.builder()
                    .timesPerPeriod(2)
                    .periodicity("daily")
                    .build();

            Reward reward = Reward.builder()
                    .points(75)
                    .badge("Champion")
                    .build();

            Progress progress = Progress.builder()
                    .completed(15)
                    .total(30)
                    .unit("days")
                    .build();

            Goal entity = Goal.builder()
                    .goalId(42L)
                    .userId("responseUser")
                    .title("Meta para Response")
                    .description("Descrição da meta para response")
                    .category(GoalCategory.NUTRICAO)
                    .type("daily")
                    .startDate(LocalDate.of(2026, 2, 15))
                    .endDate(LocalDate.of(2026, 3, 15))
                    .frequency(frequency)
                    .difficulty("medium")
                    .reward(reward)
                    .status("active")
                    .notifications(false)
                    .createdAt(LocalDateTime.of(2026, 2, 15, 9, 30))
                    .progress(progress)
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getGoalId()).isEqualTo("42");
            assertThat(result.getUserId()).isEqualTo("responseUser");
            assertThat(result.getTitle()).isEqualTo("Meta para Response");
            assertThat(result.getStatus()).isEqualTo("active");
            assertThat(result.getCreatedAt()).isEqualTo(OffsetDateTime.of(2026, 2, 15, 9, 30, 0, 0, ZoneOffset.UTC));

            // Progress validation
            assertThat(result.getProgress()).isNotNull();
            assertThat(result.getProgress().getCompleted()).isEqualTo(15);
            assertThat(result.getProgress().getTotal()).isEqualTo(30);
            assertThat(result.getProgress().getUnit()).isEqualTo("days");

            // Gamification validation
            assertThat(result.getGamification()).isNotNull();
            assertThat(result.getGamification().getPointsEarned()).isEqualTo(75);
            assertThat(result.getGamification().getBadge()).isEqualTo("Champion");
            assertThat(result.getGamification().getUserLevel()).isEqualTo(1); // Default value

            // Message validation
            assertThat(result.getMessage()).contains("Progress updated!");
            assertThat(result.getMessage()).contains("15 of 30");
            assertThat(result.getMessage()).contains("75 points");
        }
    }

    @Nested
    @DisplayName("Enum Mapping Test")
    class EnumMappingTests {

        @Test
        @DisplayName("Deve mapear todas as categorias corretamente")
        void shouldMapAllCategoriesCorrectly() {
            // Test SAUDE_FISICA
            assertThat(goalMapper.toEntity(createRequestWithCategory(GoalRequest.CategoryEnum.SAUDE_FISICA)).getCategory())
                    .isEqualTo(GoalCategory.SAUDE_FISICA);

            // Test SAUDE_MENTAL
            assertThat(goalMapper.toEntity(createRequestWithCategory(GoalRequest.CategoryEnum.SAUDE_MENTAL)).getCategory())
                    .isEqualTo(GoalCategory.SAUDE_MENTAL);

            // Test NUTRICAO
            assertThat(goalMapper.toEntity(createRequestWithCategory(GoalRequest.CategoryEnum.NUTRICAO)).getCategory())
                    .isEqualTo(GoalCategory.NUTRICAO);

            // Test SONO
            assertThat(goalMapper.toEntity(createRequestWithCategory(GoalRequest.CategoryEnum.SONO)).getCategory())
                    .isEqualTo(GoalCategory.SONO);

            // Test BEM_ESTAR
            assertThat(goalMapper.toEntity(createRequestWithCategory(GoalRequest.CategoryEnum.BEM_ESTAR)).getCategory())
                    .isEqualTo(GoalCategory.BEM_ESTAR);
        }

        @Test
        @DisplayName("Deve mapear tipos corretamente")
        void shouldMapTypesCorrectly() {
            // Test DAILY
            GoalRequest dailyRequest = createRequestWithType(GoalRequest.TypeEnum.DAILY);
            assertThat(goalMapper.toEntity(dailyRequest).getType()).isEqualTo("daily");

            // Test WEEKLY  
            GoalRequest weeklyRequest = createRequestWithType(GoalRequest.TypeEnum.WEEKLY);
            assertThat(goalMapper.toEntity(weeklyRequest).getType()).isEqualTo("weekly");

            // Test MONTHLY
            GoalRequest monthlyRequest = createRequestWithType(GoalRequest.TypeEnum.MONTHLY);
            assertThat(goalMapper.toEntity(monthlyRequest).getType()).isEqualTo("monthly");

            // Test SINGLE
            GoalRequest singleRequest = createRequestWithType(GoalRequest.TypeEnum.SINGLE);
            assertThat(goalMapper.toEntity(singleRequest).getType()).isEqualTo("single");
        }

        @Test
        @DisplayName("Deve mapear dificuldades corretamente")
        void shouldMapDifficultiesCorrectly() {
            // Test EASY
            GoalRequest easyRequest = createRequestWithDifficulty(GoalRequest.DifficultyEnum.EASY);
            assertThat(goalMapper.toEntity(easyRequest).getDifficulty()).isEqualTo("easy");

            // Test MEDIUM
            GoalRequest mediumRequest = createRequestWithDifficulty(GoalRequest.DifficultyEnum.MEDIUM);
            assertThat(goalMapper.toEntity(mediumRequest).getDifficulty()).isEqualTo("medium");

            // Test HARD
            GoalRequest hardRequest = createRequestWithDifficulty(GoalRequest.DifficultyEnum.HARD);
            assertThat(goalMapper.toEntity(hardRequest).getDifficulty()).isEqualTo("hard");
        }

        @Test
        @DisplayName("Deve mapear status corretamente")
        void shouldMapStatusCorrectly() {
            // Test ACTIVE
            GoalRequest activeRequest = createRequestWithStatus(GoalRequest.StatusEnum.ACTIVE);
            assertThat(goalMapper.toEntity(activeRequest).getStatus()).isEqualTo("active");

            // Test COMPLETED
            GoalRequest completedRequest = createRequestWithStatus(GoalRequest.StatusEnum.COMPLETED);
            assertThat(goalMapper.toEntity(completedRequest).getStatus()).isEqualTo("completed");

            // Test ARCHIVED
            GoalRequest archivedRequest = createRequestWithStatus(GoalRequest.StatusEnum.ARCHIVED);
            assertThat(goalMapper.toEntity(archivedRequest).getStatus()).isEqualTo("archived");
        }

        @Test
        @DisplayName("Deve retornar null para enums null")
        void shouldReturnNullForNullEnums() {
            // Given
            GoalRequest request = GoalRequest.builder()
                    .category(null)
                    .type(null)
                    .difficulty(null)
                    .status(null)
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result.getCategory()).isNull();
            assertThat(result.getType()).isNull();
            assertThat(result.getDifficulty()).isNull();
            assertThat(result.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("Progress Message Generation Tests")
    class ProgressMessageGenerationTests {

        @Test
        @DisplayName("Deve gerar mensagem de progresso com dados básicos")
        void shouldGenerateProgressMessageWithBasicData() {
            // Given
            Goal entityWithBasicProgress = Goal.builder()
                    .goalId(1L)
                    .progress(Progress.builder()
                            .completed(10)
                            .total(30)
                            .unit("days")
                            .build())
                    .reward(Reward.builder()
                            .points(25)
                            .build())
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entityWithBasicProgress);

            // Then
            assertThat(result.getMessage())
                    .contains("Progress updated!")
                    .contains("10 of 30 days")
                    .contains("25 points");
        }

        @Test
        @DisplayName("Deve gerar mensagem de progresso com valores zero")
        void shouldGenerateProgressMessageWithZeroValues() {
            // Given
            Goal entityWithZeroProgress = Goal.builder()
                    .goalId(1L)
                    .progress(Progress.builder()
                            .completed(0)
                            .total(0)
                            .unit("days")
                            .build())
                    .reward(Reward.builder()
                            .points(0)
                            .build())
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entityWithZeroProgress);

            // Then
            assertThat(result.getMessage())
                    .contains("Progress updated!")
                    .contains("0 of 0 days")
                    .contains("0 points");
        }

        @Test
        @DisplayName("Deve gerar mensagem com valores default quando campos são null")
        void shouldGenerateMessageWithDefaultValuesWhenFieldsAreNull() {
            // Given
            Goal entityWithNullFields = Goal.builder()
                    .goalId(1L)
                    .progress(Progress.builder()
                            .completed(null)
                            .total(null)
                            .unit(null)
                            .build())
                    .reward(null)
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entityWithNullFields);

            // Then
            assertThat(result.getMessage())
                    .contains("Progress updated!")
                    .contains("0 of 0 days")
                    .contains("0 points");
        }

        @Test
        @DisplayName("Deve usar unidade default quando unit é null")
        void shouldUseDefaultUnitWhenUnitIsNull() {
            // Given
            Goal entityWithNullUnit = Goal.builder()
                    .goalId(1L)
                    .progress(Progress.builder()
                            .completed(5)
                            .total(10)
                            .unit(null)
                            .build())
                    .reward(Reward.builder()
                            .points(15)
                            .build())
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entityWithNullUnit);

            // Then
            assertThat(result.getMessage()).contains("5 of 10 days"); // Default "days"
        }

        @Test
        @DisplayName("Deve lidar com diferentes unidades")
        void shouldHandleDifferentUnits() {
            // Given
            Goal entityWithCustomUnit = Goal.builder()
                    .goalId(1L)
                    .progress(Progress.builder()
                            .completed(3)
                            .total(7)
                            .unit("weeks")
                            .build())
                    .reward(Reward.builder()
                            .points(100)
                            .build())
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entityWithCustomUnit);

            // Then
            assertThat(result.getMessage()).contains("3 of 7 weeks");
        }
    }

    @Nested
    @DisplayName("Complex Object Mapping Tests")
    class ComplexObjectMappingTests {

        @Test
        @DisplayName("Deve mapear objetos de frequência com valores extremos")
        void shouldMapFrequencyObjectsWithExtremeValues() {
            // Given
            GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                    .timesPerPeriod(999)
                    .periodicity("extreme")
                    .build();

            GoalRequest request = GoalRequest.builder()
                    .frequency(frequency)
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result.getFrequency()).isNotNull();
            assertThat(result.getFrequency().getTimesPerPeriod()).isEqualTo(999);
            assertThat(result.getFrequency().getPeriodicity()).isEqualTo("extreme");
        }

        @Test
        @DisplayName("Deve mapear objetos de recompensa com valores negativos")
        void shouldMapRewardObjectsWithNegativeValues() {
            // Given
            GoalRequestReward reward = GoalRequestReward.builder()
                    .points(-50) // Pontos negativos (penalidade)
                    .badge("Penalty Badge")
                    .build();

            GoalRequest request = GoalRequest.builder()
                    .reward(reward)
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result.getReward()).isNotNull();
            assertThat(result.getReward().getPoints()).isEqualTo(-50);
            assertThat(result.getReward().getBadge()).isEqualTo("Penalty Badge");
        }

        @Test
        @DisplayName("Deve lidar com campos parcialmente null em objetos complexos")
        void shouldHandlePartiallyNullFieldsInComplexObjects() {
            // Given
            GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                    .timesPerPeriod(5)
                    .periodicity(null)
                    .build();

            GoalRequestReward reward = GoalRequestReward.builder()
                    .points(null)
                    .badge("Partial Badge")
                    .build();

            GoalRequest request = GoalRequest.builder()
                    .frequency(frequency)
                    .reward(reward)
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result.getFrequency()).isNotNull();
            assertThat(result.getFrequency().getTimesPerPeriod()).isEqualTo(5);
            assertThat(result.getFrequency().getPeriodicity()).isNull();

            assertThat(result.getReward()).isNotNull();
            assertThat(result.getReward().getPoints()).isNull();
            assertThat(result.getReward().getBadge()).isEqualTo("Partial Badge");
        }
    }

    @Nested
    @DisplayName("Date and Time Mapping Tests")
    class DateAndTimeMappingTests {

        @Test
        @DisplayName("Deve mapear createdAt precisamente para OffsetDateTime UTC")
        void shouldMapCreatedAtPreciselyToUTCOffsetDateTime() {
            // Given
            LocalDateTime specificDateTime = LocalDateTime.of(2026, 12, 25, 14, 30, 45);
            Goal entity = Goal.builder()
                    .goalId(1L)
                    .createdAt(specificDateTime)
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entity);

            // Then
            assertThat(result.getCreatedAt()).isEqualTo(
                    OffsetDateTime.of(2026, 12, 25, 14, 30, 45, 0, ZoneOffset.UTC)
            );
        }

        @Test
        @DisplayName("Deve lidar com createdAt null")
        void shouldHandleNullCreatedAt() {
            // Given
            Goal entity = Goal.builder()
                    .goalId(1L)
                    .createdAt(null)
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entity);

            // Then
            assertThat(result.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Deve preservar datas de início e fim")
        void shouldPreserveStartAndEndDates() {
            // Given
            LocalDate startDate = LocalDate.of(2026, 1, 15);
            LocalDate endDate = LocalDate.of(2026, 6, 15);

            GoalRequest request = GoalRequest.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result.getStartDate()).isEqualTo(startDate);
            assertThat(result.getEndDate()).isEqualTo(endDate);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Deve lidar com GoalId muito grande")
        void shouldHandleVeryLargeGoalId() {
            // Given
            Goal entity = Goal.builder()
                    .goalId(Long.MAX_VALUE)
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entity);

            // Then
            assertThat(result.getGoalId()).isEqualTo(String.valueOf(Long.MAX_VALUE));
        }

        @Test
        @DisplayName("Deve lidar com GoalId zero")
        void shouldHandleZeroGoalId() {
            // Given
            Goal entity = Goal.builder()
                    .goalId(0L)
                    .build();

            // When
            GoalResponse result = goalMapper.toResponse(entity);

            // Then
            assertThat(result.getGoalId()).isEqualTo("0");
        }

        @Test
        @DisplayName("Deve lidar com strings muito longas")
        void shouldHandleVeryLongStrings() {
            // Given
            String longString = "A".repeat(10000);
            GoalRequest request = GoalRequest.builder()
                    .userId(longString)
                    .title(longString)
                    .description(longString)
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result.getUserId()).hasSize(10000);
            assertThat(result.getTitle()).hasSize(10000);
            assertThat(result.getDescription()).hasSize(10000);
        }

        @Test
        @DisplayName("Deve lidar com strings vazias")
        void shouldHandleEmptyStrings() {
            // Given
            GoalRequest request = GoalRequest.builder()
                    .userId("")
                    .title("")
                    .description("")
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result.getUserId()).isEmpty();
            assertThat(result.getTitle()).isEmpty();
            assertThat(result.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("Deve lidar com caracteres especiais")
        void shouldHandleSpecialCharacters() {
            // Given
            String specialChars = "àáâãäåæçèéêëìíîïñòóôõöøùúûüýÿ@#$%^&*()[]{}|\\:;\"'<>,.?/~`";
            GoalRequest request = GoalRequest.builder()
                    .title(specialChars)
                    .description(specialChars)
                    .build();

            // When
            Goal result = goalMapper.toEntity(request);

            // Then
            assertThat(result.getTitle()).isEqualTo(specialChars);
            assertThat(result.getDescription()).isEqualTo(specialChars);
        }
    }

    // Helper methods

    private GoalRequest createRequestWithCategory(GoalRequest.CategoryEnum category) {
        return GoalRequest.builder()
                .userId("user123")
                .title("Test Goal")
                .category(category)
                .startDate(LocalDate.now())
                .build();
    }

    private GoalRequest createRequestWithType(GoalRequest.TypeEnum type) {
        return GoalRequest.builder()
                .userId("user123")
                .title("Test Goal")
                .type(type)
                .startDate(LocalDate.now())
                .build();
    }

    private GoalRequest createRequestWithDifficulty(GoalRequest.DifficultyEnum difficulty) {
        return GoalRequest.builder()
                .userId("user123")
                .title("Test Goal")
                .difficulty(difficulty)
                .startDate(LocalDate.now())
                .build();
    }

    private GoalRequest createRequestWithStatus(GoalRequest.StatusEnum status) {
        return GoalRequest.builder()
                .userId("user123")
                .title("Test Goal")
                .status(status)
                .startDate(LocalDate.now())
                .build();
    }
}