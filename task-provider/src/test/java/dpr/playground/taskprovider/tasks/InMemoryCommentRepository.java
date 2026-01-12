package dpr.playground.taskprovider.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class InMemoryCommentRepository implements CommentRepository {
    private final Map<UUID, Comment> comments = new HashMap<>();

    @Override
    public Comment save(Comment comment) {
        comments.put(comment.getId(), comment);
        return comment;
    }

    @Override
    public Optional<Comment> findById(UUID id) {
        return Optional.ofNullable(comments.get(id));
    }

    @Override
    public Page<Comment> findByTaskIdOrderByCreatedAtDesc(UUID taskId, Pageable pageable) {
        var allComments = new ArrayList<>(comments.values());
        var filteredComments = allComments.stream()
                .filter(c -> c.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredComments.size());
        if (start >= filteredComments.size()) {
            return new PageImpl<>(java.util.Collections.emptyList(), pageable, filteredComments.size());
        }
        return new PageImpl<>(filteredComments.subList(start, end), pageable, filteredComments.size());
    }

    @Override
    public void deleteById(UUID id) {
        comments.remove(id);
    }
}
