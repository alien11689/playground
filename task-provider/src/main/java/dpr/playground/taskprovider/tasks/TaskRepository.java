package dpr.playground.taskprovider.tasks;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface TaskRepository extends Repository<Task, UUID> {
    Task save(Task task);

    Optional<Task> findById(UUID id);

    Page<Task> findAll(Pageable pageable);

    void deleteById(UUID id);
}
