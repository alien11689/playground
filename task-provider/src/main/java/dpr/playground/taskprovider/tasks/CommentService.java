package dpr.playground.taskprovider.tasks;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dpr.playground.taskprovider.tasks.model.TaskStatusDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final Clock clock;

    public Comment createComment(UUID taskId, String content, UUID createdBy) {
        var task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            throw new IllegalArgumentException("Task not found");
        }
        if (task.get().getStatus() == TaskStatusDTO.DONE || task.get().getStatus() == TaskStatusDTO.REJECTED) {
            throw new IllegalStateException("Cannot add comments to closed tasks");
        }
        var comment = Comment.create(taskId, content, createdBy, clock);
        return commentRepository.save(comment);
    }

    public Optional<Comment> updateComment(UUID commentId, String content, UUID userId) {
        var comment = commentRepository.findById(commentId);
        if (comment.isEmpty()) {
            return Optional.empty();
        }
        if (!comment.get().getCreatedBy().equals(userId)) {
            throw new NotCommentAuthorException("Only comment author can update comment");
        }

        var task = taskRepository.findById(comment.get().getTaskId());
        if (task.isPresent()) {
            projectService.isProjectActive(task.get().getProjectId());
        }

        comment.get().update(content, clock);
        return Optional.of(commentRepository.save(comment.get()));
    }

    public void deleteComment(UUID commentId, UUID userId) {
        var comment = commentRepository.findById(commentId);
        if (comment.isEmpty()) {
            return;
        }
        if (!comment.get().getCreatedBy().equals(userId)) {
            throw new NotCommentAuthorException("Only comment author can delete comment");
        }
        commentRepository.deleteById(commentId);
    }
}
