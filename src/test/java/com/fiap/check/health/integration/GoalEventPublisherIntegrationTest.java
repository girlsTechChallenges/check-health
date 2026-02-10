package com.fiap.check.health.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.check.health.dto.event.GoalCreatedEvent;
import com.fiap.check.health.event.publisher.GoalEventPublisher;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.persistence.entity.Goal;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"goal.created"})
@ActiveProfiles("test")
@DirtiesContext
@DisplayName("GoalEventPublisher Integration Tests")
class GoalEventPublisherIntegrationTest {

    private static final String TOPIC_GOAL_CREATED = "goal.created";
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private GoalEventPublisher goalEventPublisher;
    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    @BeforeEach
    void setUp() throws InterruptedException {
        goalEventPublisher = new GoalEventPublisher(kafkaTemplate, objectMapper);
        
        // Configurar consumer para verificar mensagens
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(TOPIC_GOAL_CREATED);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        
        // Aguardar o container inicializar
        Thread.sleep(3000);
    }
    
    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    @DisplayName("Deve publicar evento Goal criado com sucesso")
    void shouldPublishGoalCreatedEventSuccessfully() throws Exception {
        // Given
        Goal goal = createValidGoal();
        
        // When
        goalEventPublisher.publishGoalCreated(goal);
        
        // Then
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertNotNull(received);
        assertThat(received.topic()).isEqualTo(TOPIC_GOAL_CREATED);
        
        GoalCreatedEvent event = objectMapper.readValue(received.value(), GoalCreatedEvent.class);
        assertThat(event.getGoalId()).isEqualTo(goal.getGoalId());
        assertThat(event.getUserId()).isEqualTo(goal.getUserId());
        assertThat(event.getCategory()).isEqualTo(goal.getCategory().name());
        assertThat(event.getTitle()).isEqualTo(goal.getTitle());
        assertThat(event.getDescription()).isEqualTo(goal.getDescription());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Goal for nulo")
    void shouldThrowExceptionWhenGoalIsNull() {
        // When & Then
        assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao publicar evento goal.created");
    }

    @Test
    @DisplayName("Deve tratar erro de serialização JSON")
    void shouldHandleJsonProcessingError() {
        // Given - Goal com propriedades que causem erro de serialização
        Goal invalidGoal = new Goal();
        invalidGoal.setGoalId(1L);
        invalidGoal.setUserId("1");
        invalidGoal.setCategory(null); // vai causar NullPointerException na serialização
        invalidGoal.setTitle("Test");
        invalidGoal.setDescription("Test");
        
        // When & Then
        assertThatThrownBy(() -> goalEventPublisher.publishGoalCreated(invalidGoal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao");
    }

    @Test
    @DisplayName("Deve publicar múltiplos eventos corretamente")
    void shouldPublishMultipleEventsCorrectly() throws Exception {
        // Given
        Goal goal1 = createValidGoal();
        goal1.setGoalId(1L);
        
        Goal goal2 = createValidGoal();
        goal2.setGoalId(2L);
        goal2.setTitle("Goal 2");
        
        // When
        goalEventPublisher.publishGoalCreated(goal1);
        goalEventPublisher.publishGoalCreated(goal2);
        
        // Then
        ConsumerRecord<String, String> received1 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> received2 = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received1);
        assertNotNull(received2);
        
        GoalCreatedEvent event1 = objectMapper.readValue(received1.value(), GoalCreatedEvent.class);
        GoalCreatedEvent event2 = objectMapper.readValue(received2.value(), GoalCreatedEvent.class);
        
        assertThat(event1.getGoalId()).isEqualTo(1L);
        assertThat(event2.getGoalId()).isEqualTo(2L);
        assertThat(event2.getTitle()).isEqualTo("Goal 2");
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