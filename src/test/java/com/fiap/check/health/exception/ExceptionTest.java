package com.fiap.check.health.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes completos para exceções personalizadas do sistema
 * 
 * Validações incluem:
 * - Construção correta de mensagens de erro
 * - Herança apropriada de RuntimeException
 * - Formatação consistente de mensagens
 */
@DisplayName("Custom Exceptions Tests")
class ExceptionTest {

    @Nested
    @DisplayName("GoalNotFoundException Tests")
    class GoalNotFoundExceptionTests {

        @Test
        @DisplayName("Deve criar exceção com ID do goal")
        void shouldCreateExceptionWithGoalId() {
            // Given
            Long goalId = 123L;

            // When
            GoalNotFoundException exception = new GoalNotFoundException(goalId);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Goal não encontrado com ID: 123");
        }

        @Test
        @DisplayName("Deve criar exceção com mensagem customizada")
        void shouldCreateExceptionWithCustomMessage() {
            // Given
            String customMessage = "Mensagem de erro personalizada";

            // When
            GoalNotFoundException exception = new GoalNotFoundException(customMessage);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Mensagem de erro personalizada");
        }

        @Test
        @DisplayName("Deve lidar com ID null corretamente")
        void shouldHandleNullIdCorrectly() {
            // Given
            Long nullId = null;

            // When
            GoalNotFoundException exception = new GoalNotFoundException(nullId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Goal não encontrado com ID: null");
        }

        @Test
        @DisplayName("Deve lidar com ID zero")
        void shouldHandleZeroId() {
            // Given
            Long zeroId = 0L;

            // When
            GoalNotFoundException exception = new GoalNotFoundException(zeroId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Goal não encontrado com ID: 0");
        }

        @Test
        @DisplayName("Deve lidar com ID negativo")
        void shouldHandleNegativeId() {
            // Given
            Long negativeId = -999L;

            // When
            GoalNotFoundException exception = new GoalNotFoundException(negativeId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Goal não encontrado com ID: -999");
        }
    }

    @Nested
    @DisplayName("GoalAlreadyCompletedException Tests")
    class GoalAlreadyCompletedExceptionTests {

        @Test
        @DisplayName("Deve criar exceção com ID do goal")
        void shouldCreateExceptionWithGoalId() {
            // Given
            Long goalId = 456L;

            // When
            GoalAlreadyCompletedException exception = new GoalAlreadyCompletedException(goalId);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Goal is already completed with ID: 456");
        }

        @Test
        @DisplayName("Deve criar exceção com mensagem customizada")
        void shouldCreateExceptionWithCustomMessage() {
            // Given
            String customMessage = "Esta meta já foi concluída anteriormente";

            // When
            GoalAlreadyCompletedException exception = new GoalAlreadyCompletedException(customMessage);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Esta meta já foi concluída anteriormente");
        }

        @Test
        @DisplayName("Deve manter consistência de formatação com outras exceções")
        void shouldMaintainFormattingConsistencyWithOtherExceptions() {
            // Given
            Long goalId = 789L;

            // When
            GoalAlreadyCompletedException exception = new GoalAlreadyCompletedException(goalId);

            // Then
            assertThat(exception.getMessage()).contains("ID: " + goalId);
            assertThat(exception.getMessage()).doesNotContain("null");
        }
    }

    @Nested
    @DisplayName("GoalAlreadyInProgressException Tests")
    class GoalAlreadyInProgressExceptionTests {

        @Test
        @DisplayName("Deve criar exceção com ID do goal")
        void shouldCreateExceptionWithGoalId() {
            // Given
            Long goalId = 101L;

            // When
            GoalAlreadyInProgressException exception = new GoalAlreadyInProgressException(goalId);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Goal is already in progress with ID: 101");
        }

        @Test
        @DisplayName("Deve criar exceção com mensagem customizada")
        void shouldCreateExceptionWithCustomMessage() {
            // Given
            String customMessage = "Esta meta já está em andamento";

            // When
            GoalAlreadyInProgressException exception = new GoalAlreadyInProgressException(customMessage);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Esta meta já está em andamento");
        }

        @Test
        @DisplayName("Deve validar estrutura de mensagem padrão")
        void shouldValidateStandardMessageStructure() {
            // Given
            Long[] testIds = {1L, 999L, 12345L};

            for (Long goalId : testIds) {
                // When
                GoalAlreadyInProgressException exception = new GoalAlreadyInProgressException(goalId);

                // Then
                assertThat(exception.getMessage())
                    .startsWith("Goal is already in progress")
                    .contains("with ID:")
                    .endsWith(goalId.toString());
            }
        }
    }

    @Nested
    @DisplayName("Exception Message Validation Tests")
    class ExceptionMessageValidationTests {

        @Test
        @DisplayName("Deve garantir que nenhuma exceção permita mensagem vazia ou null")
        void shouldEnsureNoExceptionAllowsEmptyOrNullMessage() {
            // Given & When
            GoalNotFoundException notFoundWithNull = new GoalNotFoundException((String) null);
            GoalNotFoundException notFoundWithEmpty = new GoalNotFoundException("");

            // Then
            assertThat(notFoundWithNull.getMessage()).isNull();
            assertThat(notFoundWithEmpty.getMessage()).isEmpty();
        }

        @Test
        @DisplayName("Deve validar consistência entre todas as exceções do sistema")
        void shouldValidateConsistencyBetweenAllSystemExceptions() {
            // Given
            Long testId = 555L;

            // When
            GoalNotFoundException notFound = new GoalNotFoundException(testId);
            GoalAlreadyCompletedException alreadyCompleted = new GoalAlreadyCompletedException(testId);
            GoalAlreadyInProgressException alreadyInProgress = new GoalAlreadyInProgressException(testId);

            // Then - Todas devem herdar de RuntimeException
            assertThat(notFound).isInstanceOf(RuntimeException.class);
            assertThat(alreadyCompleted).isInstanceOf(RuntimeException.class);
            assertThat(alreadyInProgress).isInstanceOf(RuntimeException.class);

            // Todas devem incluir o ID na mensagem
            assertThat(notFound.getMessage()).contains("555");
            assertThat(alreadyCompleted.getMessage()).contains("555");
            assertThat(alreadyInProgress.getMessage()).contains("555");
        }

        @Test
        @DisplayName("Deve validar mensagens para cenários de edge cases")
        void shouldValidateMessagesForEdgeCases() {
            // Given
            Long maxValue = Long.MAX_VALUE;
            Long minValue = Long.MIN_VALUE;

            // When
            GoalNotFoundException maxValueException = new GoalNotFoundException(maxValue);
            GoalNotFoundException minValueException = new GoalNotFoundException(minValue);

            // Then
            assertThat(maxValueException.getMessage()).contains(String.valueOf(maxValue));
            assertThat(minValueException.getMessage()).contains(String.valueOf(minValue));
        }
    }

    @Nested
    @DisplayName("Exception Behavior Tests")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("Deve permitir catch como RuntimeException")
        void shouldAllowCatchAsRuntimeException() {
            try {
                throw new GoalNotFoundException(999L);
            } catch (RuntimeException e) {
                assertThat(e).isInstanceOf(GoalNotFoundException.class);
                assertThat(e.getMessage()).contains("999");
            }
        }

        @Test
        @DisplayName("Deve manter stack trace corretamente")
        void shouldMaintainStackTraceCorrectly() {
            // Given & When
            GoalNotFoundException exception = new GoalNotFoundException(123L);

            // Then
            assertThat(exception.getStackTrace()).isNotEmpty();
            assertThat(exception.getStackTrace()[0].getClassName())
                .contains("ExceptionTest");
        }

        @Test
        @DisplayName("Deve suportar causa encadeada")
        void shouldSupportChainedCause() {
            // Given
            Exception cause = new IllegalArgumentException("Argumento inválido");
            
            // When
            GoalNotFoundException exception = new GoalNotFoundException("Erro encadeado");
            exception.initCause(cause);

            // Then
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getCause().getMessage()).isEqualTo("Argumento inválido");
        }
    }
}