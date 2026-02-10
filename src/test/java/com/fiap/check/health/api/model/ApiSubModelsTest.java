package com.fiap.check.health.api.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para o modelo GoalRequestFrequency (API)
 */
@DisplayName("GoalRequestFrequency Model Tests")
class GoalRequestFrequencyTest {

    @Test
    @DisplayName("Deve criar GoalRequestFrequency usando builder")
    void shouldCreateGoalRequestFrequencyUsingBuilder() {
        // When
        GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                .periodicity("weekly")
                .timesPerPeriod(3)
                .build();

        // Then
        assertThat(frequency.getPeriodicity()).isEqualTo("weekly");
        assertThat(frequency.getTimesPerPeriod()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve criar GoalRequestFrequency com valores null")
    void shouldCreateGoalRequestFrequencyWithNullValues() {
        // When
        GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                .periodicity(null)
                .timesPerPeriod(null)
                .build();

        // Then
        assertThat(frequency.getPeriodicity()).isNull();
        assertThat(frequency.getTimesPerPeriod()).isNull();
    }

    @Test
    @DisplayName("Deve ter equals e hashCode funcionais")
    void shouldHaveWorkingEqualsAndHashCode() {
        // Given
        GoalRequestFrequency frequency1 = GoalRequestFrequency.builder()
                .periodicity("daily")
                .timesPerPeriod(2)
                .build();

        GoalRequestFrequency frequency2 = GoalRequestFrequency.builder()
                .periodicity("daily")
                .timesPerPeriod(2)
                .build();

        GoalRequestFrequency frequency3 = GoalRequestFrequency.builder()
                .periodicity("weekly")
                .timesPerPeriod(2)
                .build();

        // When / Then
        assertThat(frequency1).isEqualTo(frequency2);
        assertThat(frequency1.hashCode()).isEqualTo(frequency2.hashCode());
        assertThat(frequency1).isNotEqualTo(frequency3);
    }

    @Test
    @DisplayName("Deve ter toString funcional")    
    void shouldHaveWorkingToString() {
        // Given
        GoalRequestFrequency frequency = GoalRequestFrequency.builder()
                .periodicity("monthly")
                .timesPerPeriod(1)
                .build();

        // When
        String toString = frequency.toString();

        // Then
        assertThat(toString).contains("monthly");
        assertThat(toString).contains("1");
    }

    @Test
    @DisplayName("Deve criar diferentes tipos de frequência")
    void shouldCreateDifferentFrequencyTypes() {
        // Daily frequency
        GoalRequestFrequency dailyFreq = GoalRequestFrequency.builder()
                .periodicity("daily")
                .timesPerPeriod(1)
                .build();

        // Weekly frequency  
        GoalRequestFrequency weeklyFreq = GoalRequestFrequency.builder()
                .periodicity("weekly")
                .timesPerPeriod(3)
                .build();

        // Monthly frequency
        GoalRequestFrequency monthlyFreq = GoalRequestFrequency.builder()
                .periodicity("monthly")
                .timesPerPeriod(2)
                .build();

        // Then
        assertThat(dailyFreq.getPeriodicity()).isEqualTo("daily");
        assertThat(dailyFreq.getTimesPerPeriod()).isEqualTo(1);
        
        assertThat(weeklyFreq.getPeriodicity()).isEqualTo("weekly");
        assertThat(weeklyFreq.getTimesPerPeriod()).isEqualTo(3);
        
        assertThat(monthlyFreq.getPeriodicity()).isEqualTo("monthly");
        assertThat(monthlyFreq.getTimesPerPeriod()).isEqualTo(2);
    }
}

/**
 * Testes para o modelo GoalRequestReward (API)
 */
@DisplayName("GoalRequestReward Model Tests")
class GoalRequestRewardTest {

    @Test
    @DisplayName("Deve criar GoalRequestReward usando builder")
    void shouldCreateGoalRequestRewardUsingBuilder() {
        // When
        GoalRequestReward reward = GoalRequestReward.builder()
                .points(100)
                .badge("Achievement Unlocked")
                .build();

        // Then
        assertThat(reward.getPoints()).isEqualTo(100);
        assertThat(reward.getBadge()).isEqualTo("Achievement Unlocked");
    }

    @Test
    @DisplayName("Deve criar GoalRequestReward apenas com pontos")
    void shouldCreateGoalRequestRewardWithPointsOnly() {
        // When
        GoalRequestReward reward = GoalRequestReward.builder()
                .points(50)
                .build();

        // Then
        assertThat(reward.getPoints()).isEqualTo(50);
        assertThat(reward.getBadge()).isNull();
    }

    @Test
    @DisplayName("Deve criar GoalRequestReward apenas com badge")
    void shouldCreateGoalRequestRewardWithBadgeOnly() {
        // When
        GoalRequestReward reward = GoalRequestReward.builder()
                .badge("Fitness Master")
                .build();

        // Then
        assertThat(reward.getPoints()).isNull();
        assertThat(reward.getBadge()).isEqualTo("Fitness Master");
    }

    @Test
    @DisplayName("Deve criar GoalRequestReward com valores null")
    void shouldCreateGoalRequestRewardWithNullValues() {
        // When
        GoalRequestReward reward = GoalRequestReward.builder()
                .points(null)
                .badge(null)
                .build();

        // Then
        assertThat(reward.getPoints()).isNull();
        assertThat(reward.getBadge()).isNull();
    }

    @Test
    @DisplayName("Deve ter equals e hashCode funcionais")
    void shouldHaveWorkingEqualsAndHashCode() {
        // Given
        GoalRequestReward reward1 = GoalRequestReward.builder()
                .points(200)
                .badge("Champion")
                .build();

        GoalRequestReward reward2 = GoalRequestReward.builder()
                .points(200)
                .badge("Champion")
                .build();

        GoalRequestReward reward3 = GoalRequestReward.builder()
                .points(100)
                .badge("Champion")
                .build();

        // When / Then
        assertThat(reward1).isEqualTo(reward2);
        assertThat(reward1.hashCode()).isEqualTo(reward2.hashCode());
        assertThat(reward1).isNotEqualTo(reward3);
    }

    @Test
    @DisplayName("Deve ter toString funcional")
    void shouldHaveWorkingToString() {
        // Given
        GoalRequestReward reward = GoalRequestReward.builder()
                .points(75)
                .badge("Health Hero")
                .build();

        // When
        String toString = reward.toString();

        // Then
        assertThat(toString).contains("75");
        assertThat(toString).contains("Health Hero");
    }

    @Test
    @DisplayName("Deve criar diferentes tipos de recompensa")
    void shouldCreateDifferentRewardTypes() {
        // High value reward
        GoalRequestReward highValueReward = GoalRequestReward.builder()
                .points(1000)
                .badge("Elite Performer")
                .build();

        // Low value reward
        GoalRequestReward lowValueReward = GoalRequestReward.builder()
                .points(10)
                .badge("Getting Started")
                .build();

        // Special badge only
        GoalRequestReward specialBadge = GoalRequestReward.builder()
                .badge("Special Achievement")
                .build();

        // Points only
        GoalRequestReward pointsOnly = GoalRequestReward.builder()
                .points(500)
                .build();

        // Then
        assertThat(highValueReward.getPoints()).isEqualTo(1000);
        assertThat(highValueReward.getBadge()).isEqualTo("Elite Performer");
        
        assertThat(lowValueReward.getPoints()).isEqualTo(10);
        assertThat(lowValueReward.getBadge()).isEqualTo("Getting Started");
        
        assertThat(specialBadge.getPoints()).isNull();
        assertThat(specialBadge.getBadge()).isEqualTo("Special Achievement");
        
        assertThat(pointsOnly.getPoints()).isEqualTo(500);
        assertThat(pointsOnly.getBadge()).isNull();
    }
}