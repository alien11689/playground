package dpr.playground.taskprovider.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class InMemoryTaskRepository implements TaskRepository {
    private final Map<UUID, Task> tasks = new HashMap<>();

    @Override
    public Task save(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task findById(UUID id) {
        return tasks.get(id);
    }

    @Override
    public Page<Task> findAll(Pageable pageable) {
        var allTasks = new java.util.ArrayList<>(tasks.values());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allTasks.size());
        if (start >= allTasks.size()) {
            return new PageImpl<>(java.util.Collections.emptyList(), pageable, tasks.size());
        }
        return new PageImpl<>(allTasks.subList(start, end), pageable, tasks.size());
    }

    @Override
    public void deleteById(UUID id) {
        tasks.remove(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return tasks.containsKey(id);
    }
}
