package dpr.playground.taskprovider;

import dpr.playground.taskprovider.tasks.api.TasksApi;
import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequest;
import dpr.playground.taskprovider.tasks.model.AddTaskRequest;
import dpr.playground.taskprovider.tasks.model.Comment;
import dpr.playground.taskprovider.tasks.model.GetTaskCommentsResponse;
import dpr.playground.taskprovider.tasks.model.GetTasksResponse;
import dpr.playground.taskprovider.tasks.model.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TasksController implements TasksApi {
    @Override
    public ResponseEntity<Task> addTask(AddTaskRequest addTaskRequest) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<Comment> addTaskComment(UUID taskId, AddTaskCommentRequest addTaskCommentRequest) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deleteTaskComment(UUID taskId, UUID commentId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<Task> getTask(UUID taskId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<GetTaskCommentsResponse> getTaskComments(UUID taskId, Integer page, Integer size, Pageable pageable) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<GetTasksResponse> getTasks(Integer page, Integer size, Pageable pageable) {
        // TODO finish
        return ResponseEntity.ok(new GetTasksResponse());
    }

    @Override
    public ResponseEntity<Void> updateTask(UUID taskId, Task task) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> updateTaskComment(UUID taskId, UUID commentId, Comment comment) {
        throw new RuntimeException("Not implemented");
    }
}
