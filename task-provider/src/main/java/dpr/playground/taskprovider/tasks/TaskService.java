package dpr.playground.taskprovider.tasks;

import java.time.Clock;
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

    public Task updateTask(UUID id, UpdateTaskCommand command) {
        var task = taskRepository.findById(id);
        if (task == null) {
            return null;
        }

        task.update(command, clock);
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}

