package dpr.playground.taskprovider.endpoint;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dpr.playground.taskprovider.tasks.api.TasksApi;
import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequestDTO;
import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.CommentDTO;
import dpr.playground.taskprovider.tasks.model.GetTaskCommentsResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;

@RestController
class TasksController implements TasksApi {
    @Override
    public ResponseEntity<TaskDTO> addTask(AddTaskRequestDTO addTaskRequest) {
        // TODO implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<CommentDTO> addTaskComment(UUID taskId, AddTaskCommentRequestDTO addTaskCommentRequest) {
        // TODO implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deleteTaskComment(UUID taskId, UUID commentId) {
        // TODO implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<TaskDTO> getTask(UUID taskId) {
        // TODO implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<GetTaskCommentsResponseDTO> getTaskComments(UUID taskId, Integer page, Integer size) {
        // TODO implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<GetTasksResponseDTO> getTasks(Integer page, Integer size) {
        // TODO implement
        return ResponseEntity.ok(new GetTasksResponseDTO());
    }

    @Override
    public ResponseEntity<Void> updateTask(UUID taskId, TaskDTO task) {
        // TODO implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> updateTaskComment(UUID taskId, UUID commentId, CommentDTO comment) {
        // TODO implement
        throw new RuntimeException("Not implemented");
    }
}
