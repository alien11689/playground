package dpr.playground.taskprovider;

import dpr.playground.taskprovider.tasks.api.TasksApi;
import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequestDTO;
import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.CommentDTO;
import dpr.playground.taskprovider.tasks.model.GetTaskCommentsResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TasksController implements TasksApi {
    @Override
    public ResponseEntity<TaskDTO> addTask(AddTaskRequestDTO addTaskRequest) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<CommentDTO> addTaskComment(UUID taskId, AddTaskCommentRequestDTO addTaskCommentRequest) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deleteTaskComment(UUID taskId, UUID commentId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<TaskDTO> getTask(UUID taskId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<GetTaskCommentsResponseDTO> getTaskComments(UUID taskId, Integer page, Integer size, Pageable pageable) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<GetTasksResponseDTO> getTasks(Integer page, Integer size, Pageable pageable) {
        // TODO finish
        return ResponseEntity.ok(new GetTasksResponseDTO());
    }

    @Override
    public ResponseEntity<Void> updateTask(UUID taskId, TaskDTO task) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> updateTaskComment(UUID taskId, UUID commentId, CommentDTO comment) {
        throw new RuntimeException("Not implemented");
    }
}
