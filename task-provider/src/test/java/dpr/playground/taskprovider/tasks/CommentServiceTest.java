package dpr.playground.taskprovider.tasks;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import dpr.playground.taskprovider.tasks.model.TaskStatusDTO;
import dpr.playground.taskprovider.tasks.NotCommentAuthorException;
import dpr.playground.taskprovider.tasks.NotCommentAuthorException;

class CommentServiceTest {
    private CommentService commentService;
    private InMemoryCommentRepository commentRepository;
    private InMemoryTaskRepository taskRepository;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        commentRepository = new InMemoryCommentRepository();
        taskRepository = new InMemoryTaskRepository();
        fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));
        commentService = new CommentService(commentRepository, taskRepository, fixedClock);
    }

    @Test
    void createComment_shouldCreateComment() {
        var taskId = createTaskWithStatus(TaskStatusDTO.NEW);
        var userId = UUID.randomUUID();
        var content = "Test comment";

        var comment = commentService.createComment(taskId, content, userId);

        assertNotNull(comment);
        assertNotNull(comment.getId());
        assertEquals(taskId, comment.getTaskId());
        assertEquals(content, comment.getContent());
        assertEquals(userId, comment.getCreatedBy());
        assertNotNull(comment.getCreatedAt());
        assertNotNull(comment.getUpdatedAt());
    }

    @Test
    void createComment_shouldThrowWhenTaskNotFound() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(taskId, "Comment", userId);
        });
    }

    @Test
    void createComment_shouldThrowWhenTaskIsDone() {
        var taskId = createTaskWithStatus(TaskStatusDTO.DONE);
        var userId = UUID.randomUUID();

        assertThrows(IllegalStateException.class, () -> {
            commentService.createComment(taskId, "Comment", userId);
        });
    }

    @Test
    void createComment_shouldThrowWhenTaskIsRejected() {
        var taskId = createTaskWithStatus(TaskStatusDTO.REJECTED);
        var userId = UUID.randomUUID();

        assertThrows(IllegalStateException.class, () -> {
            commentService.createComment(taskId, "Comment", userId);
        });
    }

    @Test
    void updateComment_shouldUpdateContent() {
        var taskId = createTaskWithStatus(TaskStatusDTO.NEW);
        var userId = UUID.randomUUID();
        var comment = commentService.createComment(taskId, "Original content", userId);

        var updatedComment = commentService.updateComment(comment.getId(), "Updated content", userId);

        assertTrue(updatedComment.isPresent());
        assertEquals("Updated content", updatedComment.get().getContent());
        assertEquals(comment.getCreatedAt(), updatedComment.get().getCreatedAt());
        assertNotNull(updatedComment.get().getUpdatedAt());
    }

    @Test
    void updateComment_shouldThrowWhenNotAuthor() {
        var taskId = createTaskWithStatus(TaskStatusDTO.NEW);
        var authorId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        var comment = commentService.createComment(taskId, "Content", authorId);

        assertThrows(NotCommentAuthorException.class, () -> {
            commentService.updateComment(comment.getId(), "Updated", otherUserId);
        });
    }

    @Test
    void updateComment_shouldReturnEmptyWhenNotFound() {
        var result = commentService.updateComment(UUID.randomUUID(), "Content", UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteComment_shouldDeleteComment() {
        var taskId = createTaskWithStatus(TaskStatusDTO.NEW);
        var userId = UUID.randomUUID();
        var comment = commentService.createComment(taskId, "Content", userId);

        commentService.deleteComment(comment.getId(), userId);

        assertTrue(commentRepository.findById(comment.getId()).isEmpty());
    }

    @Test
    void deleteComment_shouldThrowWhenNotAuthor() {
        var taskId = createTaskWithStatus(TaskStatusDTO.NEW);
        var authorId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        var comment = commentService.createComment(taskId, "Content", authorId);

        assertThrows(NotCommentAuthorException.class, () -> {
            commentService.deleteComment(comment.getId(), otherUserId);
        });
    }

    @Test
    void deleteComment_shouldNotThrowWhenNotFound() {
        assertDoesNotThrow(() -> {
            commentService.deleteComment(UUID.randomUUID(), UUID.randomUUID());
        });
    }

    private UUID createTaskWithStatus(TaskStatusDTO status) {
        var command = new CreateTaskCommand("Test task", "Description", UUID.randomUUID());
        var task = new TaskService(taskRepository, fixedClock).createTask(command);
        if (status != TaskStatusDTO.NEW) {
            var updateCommand = new UpdateTaskCommand(
                    java.util.Optional.empty(),
                    java.util.Optional.empty(),
                    java.util.Optional.of(status),
                    java.util.Optional.empty(),
                    UUID.randomUUID());
            new TaskService(taskRepository, fixedClock).updateTask(task.getId(), updateCommand);
        }
        return task.getId();
    }
}
