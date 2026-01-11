package dpr.playground.taskprovider.tasks;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final Clock clock;

    TaskService(TaskRepository taskRepository, Clock clock) {
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    public Task createTask(CreateTaskCommand command) {
        var task = Task.create(command, clock);
        return taskRepository.save(task);
    }

    public Optional<Task> updateTask(UUID id, UpdateTaskCommand command) {
        var task = taskRepository.findById(id);
        if (task.isEmpty()) {
            return Optional.empty();
        }

        task.get().update(command, clock);
        return Optional.of(taskRepository.save(task.get()));
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}

