package dpr.playground.taskprovider.endpoint;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import dpr.playground.taskprovider.auth.LoggedUser;
import dpr.playground.taskprovider.tasks.api.TasksApi;
import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequestDTO;
import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.CommentDTO;
import dpr.playground.taskprovider.tasks.model.GetTaskCommentsResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;
import dpr.playground.taskprovider.tasks.model.TaskStatusDTO;
import dpr.playground.taskprovider.tasks.Task;
import dpr.playground.taskprovider.tasks.TaskMapper;
import dpr.playground.taskprovider.tasks.TaskRepository;
import dpr.playground.taskprovider.tasks.TaskService;
import dpr.playground.taskprovider.tasks.CommentService;
import dpr.playground.taskprovider.tasks.CommentRepository;
import dpr.playground.taskprovider.tasks.CommentMapper;
import dpr.playground.taskprovider.tasks.CreateTaskCommand;
import dpr.playground.taskprovider.tasks.UpdateTaskCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
class TasksController implements TasksApi {
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    private LoggedUser getCurrentUser() {
        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public ResponseEntity<TaskDTO> addTask(AddTaskRequestDTO addTaskRequest) {
        var currentUser = getCurrentUser();

        var command = new CreateTaskCommand(
                addTaskRequest.getSummary(),
                addTaskRequest.getDescription(),
                currentUser.getId(),
                addTaskRequest.getProjectId());
        var task = taskService.createTask(command);
        return new ResponseEntity<>(taskMapper.toDtoWithAssignee(task), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CommentDTO> addTaskComment(UUID taskId, AddTaskCommentRequestDTO addTaskCommentRequest) {
        var currentUser = getCurrentUser();
        var comment = commentService.createComment(taskId, addTaskCommentRequest.getContent(), currentUser.getId());
        return new ResponseEntity<>(commentMapper.toDto(comment), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteTaskComment(UUID taskId, UUID commentId) {
        var currentUser = getCurrentUser();
        var existingComment = commentRepository.findById(commentId);
        if (existingComment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        commentService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TaskDTO> getTask(UUID taskId) {
        var task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(taskMapper.toDtoWithAssignee(task.get()));
    }

    @Override
    public ResponseEntity<GetTaskCommentsResponseDTO> getTaskComments(UUID taskId, Integer page, Integer size) {
        var task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        var commentsPage = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
        return ResponseEntity.ok(commentMapper.toGetTaskCommentsResponse(commentsPage));
    }

    @Override
    public ResponseEntity<GetTasksResponseDTO> getTasks(Integer page, Integer size, UUID projectId) {
        var pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        var tasksPage = taskRepository.findByProjectId(projectId, pageable);
        return ResponseEntity.ok(taskMapper.toGetTasksResponse(tasksPage));
    }

    @Override
    public ResponseEntity<Void> updateTask(UUID taskId, TaskDTO task) {
        var currentUser = getCurrentUser();
        var existingTask = taskRepository.findById(taskId);
        if (existingTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var command = new UpdateTaskCommand(
                Optional.ofNullable(task.getSummary()),
                Optional.ofNullable(task.getDescription()),
                Optional.ofNullable(task.getStatus()),
                Optional.ofNullable(task.getAssignee()),
                currentUser.getId());

        taskService.updateTask(taskId, command);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateTaskComment(UUID taskId, UUID commentId, CommentDTO comment) {
        var currentUser = getCurrentUser();
        var updatedComment = commentService.updateComment(commentId, comment.getContent(), currentUser.getId());
        if (updatedComment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

