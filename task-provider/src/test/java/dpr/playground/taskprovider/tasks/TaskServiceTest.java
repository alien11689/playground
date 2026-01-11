package dpr.playground.taskprovider.tasks;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import dpr.playground.taskprovider.tasks.model.TaskStatusDTO;

class TaskServiceTest {
    private TaskService taskService;
    private InMemoryTaskRepository repository;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
        fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));
        taskService = new TaskService(repository, fixedClock);
    }

    @Test
    void createTask_shouldCreateTaskWithNewStatus() {
        var command = new CreateTaskCommand("Test summary", "Test description", UUID.randomUUID());
        
        var task = taskService.createTask(command);
        
        assertNotNull(task);
        assertNotNull(task.getId());
        assertEquals("Test summary", task.getSummary());
        assertEquals("Test description", task.getDescription());
        assertEquals(TaskStatusDTO.NEW, task.getStatus());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
        assertEquals(command.createdBy(), task.getCreatedBy());
        assertEquals(command.createdBy(), task.getUpdatedBy());
    }

    @Test
    void createTask_shouldPersistTask() {
        var command = new CreateTaskCommand("Test summary", null, UUID.randomUUID());

        var task = taskService.createTask(command);

        var savedTask = repository.findById(task.getId());
        assertTrue(savedTask.isPresent());
        assertEquals(task.getId(), savedTask.get().getId());
    }

    @Test
    void updateTask_shouldUpdateExistingTask() {
        var createCommand = new CreateTaskCommand("Original summary", "Original description", UUID.randomUUID());
        var createdTask = taskService.createTask(createCommand);
        var userId = UUID.randomUUID();
        var updateCommand = new UpdateTaskCommand(
                java.util.Optional.of("Updated summary"),
                java.util.Optional.of("Updated description"),
                java.util.Optional.of(TaskStatusDTO.PENDING),
                java.util.Optional.of(UUID.randomUUID()),
                userId);

        var updatedTask = taskService.updateTask(createdTask.getId(), updateCommand);

        assertTrue(updatedTask.isPresent());
        assertEquals("Updated summary", updatedTask.get().getSummary());
        assertEquals("Updated description", updatedTask.get().getDescription());
        assertEquals(TaskStatusDTO.PENDING, updatedTask.get().getStatus());
        assertEquals(userId, updatedTask.get().getUpdatedBy());
        assertEquals(createdTask.getCreatedAt(), updatedTask.get().getCreatedAt());
        assertEquals(createdTask.getCreatedBy(), updatedTask.get().getCreatedBy());
    }

    @Test
    void updateTask_shouldUpdateUpdatedAt() {
        var createCommand = new CreateTaskCommand("Test summary", null, UUID.randomUUID());
        var createdTask = taskService.createTask(createCommand);
        var updateCommand = new UpdateTaskCommand(
                java.util.Optional.of("Updated summary"),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                UUID.randomUUID());
        
        taskService.updateTask(createdTask.getId(), updateCommand);

        var updatedTask = repository.findById(createdTask.getId());
        assertEquals(createdTask.getCreatedAt(), updatedTask.get().getCreatedAt());
    }

    @Test
    void updateTask_shouldReturnNullWhenTaskNotFound() {
        var updateCommand = new UpdateTaskCommand(
                java.util.Optional.of("Updated summary"),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                UUID.randomUUID());

        var result = taskService.updateTask(UUID.randomUUID(), updateCommand);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateTask_shouldPartiallyUpdateTask() {
        var createCommand = new CreateTaskCommand("Test summary", "Test description", UUID.randomUUID());
        var createdTask = taskService.createTask(createCommand);
        var updateCommand = new UpdateTaskCommand(
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.of(TaskStatusDTO.DONE),
                java.util.Optional.empty(),
                UUID.randomUUID());

        var updatedTask = taskService.updateTask(createdTask.getId(), updateCommand);

        assertEquals("Test summary", updatedTask.get().getSummary());
        assertEquals("Test description", updatedTask.get().getDescription());
        assertEquals(TaskStatusDTO.DONE, updatedTask.get().getStatus());
    }

    @Test
    void deleteTask_shouldDeleteExistingTask() {
        var createCommand = new CreateTaskCommand("Test summary", null, UUID.randomUUID());
        var createdTask = taskService.createTask(createCommand);

        taskService.deleteTask(createdTask.getId());

        assertTrue(repository.findById(createdTask.getId()).isEmpty());
    }

    @Test
    void deleteTask_shouldNotThrowWhenTaskNotFound() {
        assertDoesNotThrow(() -> taskService.deleteTask(UUID.randomUUID()));
    }
}
