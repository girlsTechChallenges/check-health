package com.fiap.check.health.integration;

import com.fiap.check.health.persistence.entity.Goal;
import com.fiap.check.health.persistence.repository.GoalRepository;
import com.fiap.check.health.model.GoalCategory;
import com.fiap.check.health.model.Progress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de Integração Super Simples para GoalRepository
 * 
 * Usa @SpringBootTest para testar funcionalidades básicas do Repository
 * com banco H2 em memória
 * 
 * Cenários testados:
 * - Persistência e consulta de metas
 * - Validação de repositories
 * - Consultas básicas
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Goal Repository Super Simple Tests")
class GoalRepositorySimpleTest {

    @Autowired
    private GoalRepository goalRepository;

    @Test
    @DisplayName("Deve conseguir injetar o GoalRepository")
    void deveConseguirInjetarGoalRepository() {
        // Given/When - Repository injetado pelo Spring
        
        // Then - Repository não deve ser nulo
        assertThat(goalRepository).isNotNull();
    }

    @Test
    @DisplayName("Deve conseguir salvar meta simples")
    void deveConseguirSalvarMetaSimples() {
        // Given - Meta básica para teste
        Goal goal = new Goal();
        goal.setUserId("user123");
        goal.setTitle("Meta Teste");
        goal.setDescription("Teste de persistência");
        goal.setCategory(GoalCategory.SAUDE_FISICA);
        goal.setType("daily");
        goal.setStartDate(LocalDate.now());
        goal.setStatus("active");
        goal.setNotifications(true);
        
        // Inicializar progress se necessário
        Progress progress = new Progress();
        progress.setCompleted(0);
        progress.setTotal(30);
        goal.setProgress(progress);

        // When - Salvar meta
        Goal savedGoal = goalRepository.save(goal);

        // Then - Meta deve ter sido salva com sucesso
        assertThat(savedGoal).isNotNull();
        assertThat(savedGoal.getGoalId()).isNotNull();
    }

    @Test
    @DisplayName("Deve conseguir buscar meta salva") 
    void deveConseguirBuscarMetaSalva() {
        // Given - Meta salva no banco
        Goal goal = createSimpleGoal("user456", "Meta Buscar");
        Goal savedGoal = goalRepository.save(goal);

        // When - Buscar meta por ID
        Optional<Goal> foundGoal = goalRepository.findById(savedGoal.getGoalId());

        // Then - Meta deve ser encontrada
        assertThat(foundGoal).isPresent();
        assertThat(foundGoal.get().getTitle()).isEqualTo("Meta Buscar");
        assertThat(foundGoal.get().getUserId()).isEqualTo("user456");
    }

    @Test
    @DisplayName("Deve listar todas as metas")
    void deveListarTodasAsMetas() {
        // Given - Múltiplas metas salvas
        goalRepository.save(createSimpleGoal("user1", "Meta 1"));
        goalRepository.save(createSimpleGoal("user2", "Meta 2"));
        goalRepository.save(createSimpleGoal("user3", "Meta 3"));

        // When - Listar todas
        List<Goal> allGoals = goalRepository.findAll();

        // Then - Deve retornar 3 metas
        assertThat(allGoals).hasSize(3);
    }

    @Test
    @DisplayName("Deve encontrar metas por usuário")
    void deveEncontrarMetasPorUsuario() {
        // Given - Metas de múltiplos usuários
        goalRepository.save(createSimpleGoal("user123", "Meta User 123 - A"));
        goalRepository.save(createSimpleGoal("user123", "Meta User 123 - B"));
        goalRepository.save(createSimpleGoal("user999", "Meta User 999"));

        // When - Buscar metas do user123
        List<Goal> userGoals = goalRepository.findByUserId("user123");

        // Then - Deve retornar apenas metas do usuário correto
        assertThat(userGoals).hasSize(2);
        assertThat(userGoals).allMatch(goal -> "user123".equals(goal.getUserId()));
    }

    @Test
    @DisplayName("Deve retornar lista vazia para usuário sem metas")
    void deveRetornarListaVaziaParaUsuarioSemMetas() {
        // Given - Algumas metas no banco mas nenhuma do usuário teste
        goalRepository.save(createSimpleGoal("user1", "Meta qualquer"));

        // When - Buscar metas de usuário inexistente
        List<Goal> userGoals = goalRepository.findByUserId("usuario_inexistente");

        // Then - Lista deve estar vazia
        assertThat(userGoals).isEmpty();
    }

    // Método auxiliar para criar metas simples
    private Goal createSimpleGoal(String userId, String title) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setDescription("Descrição: " + title);
        goal.setCategory(GoalCategory.SAUDE_FISICA);
        goal.setType("weekly");
        goal.setStartDate(LocalDate.now());
        goal.setStatus("active");
        goal.setNotifications(true);
        
        // Progress simples
        Progress progress = new Progress();
        progress.setCompleted(0);
        progress.setTotal(10);
        goal.setProgress(progress);
        
        return goal;
    }
}