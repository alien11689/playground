package dpr.playground.taskprovider.tasks;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Task save(Task task);

    Optional<Task> findById(UUID id);

    Page<Task> findAll(Pageable pageable);

    void deleteById(UUID id);

    @Query("SELECT t FROM Task t WHERE (:projectId IS NULL OR t.project.id = :projectId)")
    Page<Task> findByProjectId(UUID projectId, Pageable pageable);
}
