package dpr.playground.taskprovider.tasks;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface CommentRepository extends Repository<Comment, UUID> {
    Comment save(Comment comment);

    Optional<Comment> findById(UUID id);

    Page<Comment> findByTaskIdOrderByCreatedAtDesc(UUID taskId, Pageable pageable);

    void deleteById(UUID id);
}
