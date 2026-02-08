package com.fiap.check.health.mapper;

import com.fiap.check.health.api.model.GoalRequest;
import com.fiap.check.health.api.model.GoalRequestFrequency;
import com.fiap.check.health.api.model.GoalRequestReward;
import com.fiap.check.health.api.model.GoalResponse;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.persistence.entity.Goal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoalMapper Tests")
class GoalMapperTest {

    private GoalMapper goalMapper;
    private GoalRequest goalRequest;
    private Goal goalEntity;

    @BeforeEach
    void setUp() {
        goalMapper = new GoalMapper();
        
        // Preparar o GoalRequest mock com todos os campos
        GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                .timesPerPeriod(1)
                .periodicity("daily")
                .build();
        
        GoalRequestReward reward = GoalRequestReward.builder()
                .points(10)
                .badge("Parabéns por completar!")
                .build();

        goalRequest = GoalRequest.builder()
                .userId("user123")
                .title("Exercitar-se diariamente")
                .description("Fazer 30 minutos de exercícios por dia")
                .startDate(LocalDate.of(2026, 2, 8))
                .endDate(LocalDate.of(2026, 3, 10))
                .frequency(frequency)
                .reward(reward)
                .notifications(true)
                .build();

        // Preparar a entidade Goal com todos os campos
        goalEntity = Goal.builder()
                .goalId(1L)
                .userId("user123")
                .title("Exercitar-se diariamente")
                .description("Fazer 30 minutos de exercícios por dia")
                .category(GoalCategory.SAUDE_FISICA)
                .type("daily")
                .startDate(LocalDate.of(2026, 2, 8))
                .endDate(LocalDate.of(2026, 3, 10))
                .status("active")
                .notifications(true)
                .createdAt(LocalDateTime.of(2026, 2, 8, 10, 30))
                .build();
    }

    @Test
    @DisplayName("Deve converter GoalRequest para Goal entity com sucesso")
    void shouldConvertGoalRequestToEntitySuccessfully() {
        // When
        Goal result = goalMapper.toEntity(goalRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getTitle()).isEqualTo("Exercitar-se diariamente");
        assertThat(result.getDescription()).isEqualTo("Fazer 30 minutos de exercícios por dia");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 8));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(result.getNotifications()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar null quando GoalRequest for null")
    void shouldReturnNullWhenGoalRequestIsNull() {
        // When
        Goal result = goalMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve converter Goal entity para GoalResponse com sucesso")
    void shouldConvertEntityToGoalResponseSuccessfully() {
        // When
        GoalResponse result = goalMapper.toResponse(goalEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGoalId()).isEqualTo("1");
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getTitle()).isEqualTo("Exercitar-se diariamente");
        assertThat(result.getStatus()).isEqualTo("active");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar null quando Goal entity for null")
    void shouldReturnNullWhenGoalEntityIsNull() {
        // When
        GoalResponse result = goalMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve lidar com campos opcionais nulos corretamente")
    void shouldHandleOptionalNullFieldsCorrectly() {
        // Given
        GoalRequest requestWithNulls = GoalRequest.builder()
                .userId("user123")
                .title("Meta simples")
                .startDate(LocalDate.now())
                .notifications(false)
                // Campos opcionais deixados como null
                .description(null)
                .endDate(null)
                .frequency(null)
                .reward(null)
                .build();

        // When
        Goal result = goalMapper.toEntity(requestWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getTitle()).isEqualTo("Meta simples");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getEndDate()).isNull();
        assertThat(result.getFrequency()).isNull();
        assertThat(result.getReward()).isNull();
        assertThat(result.getNotifications()).isFalse();
    }

    @Test
    @DisplayName("Deve converter goalId null para string null em GoalResponse")
    void shouldConvertNullGoalIdToNullStringInResponse() {
        // Given
        Goal entityWithNullId = Goal.builder()
                .goalId(null)
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
                .build();

        // When
        GoalResponse result = goalMapper.toResponse(entityWithNullId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGoalId()).isNull();
    }
}