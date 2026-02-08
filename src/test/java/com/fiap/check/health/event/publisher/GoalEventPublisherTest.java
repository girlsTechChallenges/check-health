package com.fiap.check.health.event.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.persistence.entity.Goal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalEventPublisher Unit Tests")
class GoalEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CompletableFuture<SendResult<String, String>> future;

    private GoalEventPublisher goalEventPublisher;

    @BeforeEach
    void setUp() {
        goalEventPublisher = new GoalEventPublisher(kafkaTemplate, objectMapper);
    }

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessScenarios {

        @Test
        @DisplayName("Deve publicar evento com sucesso quando Goal válido")
        void shouldPublishEventSuccessfullyWhenValidGoal() throws Exception {
            // Given
            Goal goal = createValidGoal();
            String jsonEvent = "{\"goalId\":1,\"userId\":100}";
            
            when(objectMapper.writeValueAsString(any())).thenReturn(jsonEvent);
            when(kafkaTemplate.send(eq("goal.created"), eq(jsonEvent))).thenReturn(future);

            // When
            goalEventPublisher.publishGoalCreated(goal);

            // Then
            verify(objectMapper).writeValueAsString(any());
            verify(kafkaTemplate).send("goal.created", jsonEvent);
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {

        @Test
        @DisplayName("Deve lançar RuntimeException quando ObjectMapper falha na serialização")
        void shouldThrowRuntimeExceptionWhenObjectMapperFails() throws Exception {
            // Given
            Goal goal = createValidGoal();
            when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization error") {});

            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(goal))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Falha ao serializar evento goal.created")
                    .hasCauseInstanceOf(JsonProcessingException.class);

            verify(objectMapper).writeValueAsString(any());
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando KafkaTemplate falha")
        void shouldThrowRuntimeExceptionWhenKafkaTemplateFails() throws Exception {
            // Given
            Goal goal = createValidGoal();
            String jsonEvent = "{\"goalId\":1,\"userId\":100}";
            
            when(objectMapper.writeValueAsString(any())).thenReturn(jsonEvent);
            when(kafkaTemplate.send(anyString(), anyString())).thenThrow(new RuntimeException("Kafka connection error"));

            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(goal))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Falha ao publicar evento goal.created")
                    .hasCauseInstanceOf(RuntimeException.class);

            verify(objectMapper).writeValueAsString(any());
            verify(kafkaTemplate).send("goal.created", jsonEvent);
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando Goal for nulo")
        void shouldThrowRuntimeExceptionWhenGoalIsNull() {
            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Falha ao publicar evento goal.created");
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando Goal tem propriedades nulas")
        void shouldThrowRuntimeExceptionWhenGoalHasNullProperties() {
            // Given
            Goal goalWithNullCategory = createValidGoal();
            goalWithNullCategory.setCategory(null);

            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(goalWithNullCategory))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Falha ao publicar evento goal.created");
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando ocorre erro genérico")
        void shouldThrowRuntimeExceptionWhenGenericErrorOccurs() throws Exception {
            // Given
            Goal goal = createValidGoal();
            when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Generic runtime error"));

            // When & Then
            assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(goal))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Falha ao publicar evento goal.created");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Deve publicar evento quando Goal tem valores extremos")
        void shouldPublishEventWhenGoalHasExtremeValues() throws Exception {
            // Given
            Goal goal = new Goal();
            goal.setGoalId(999L);
            goal.setUserId("999");
            goal.setCategory(GoalCategory.SAUDE_FISICA);
            goal.setTitle("A".repeat(100)); // Título longo
            goal.setDescription("B".repeat(500)); // Descrição longa
            goal.setCreatedAt(LocalDateTime.now());
            goal.setStatus("active");
            
            String jsonEvent = "{\"goalId\":999}";
            when(objectMapper.writeValueAsString(any())).thenReturn(jsonEvent);
            when(kafkaTemplate.send(eq("goal.created"), eq(jsonEvent))).thenReturn(future);

            // When
            goalEventPublisher.publishGoalCreated(goal);

            // Then
            verify(objectMapper).writeValueAsString(any());
            verify(kafkaTemplate).send("goal.created", jsonEvent);
        }
    }

    private Goal createValidGoal() {
        Goal goal = new Goal();
        goal.setGoalId(1L);
        goal.setUserId("100");
        goal.setCategory(GoalCategory.SAUDE_FISICA);
        goal.setTitle("Test Goal");
        goal.setDescription("Test Description");
        goal.setCreatedAt(LocalDateTime.now());
        goal.setStatus("active");
        return goal;
    }
}