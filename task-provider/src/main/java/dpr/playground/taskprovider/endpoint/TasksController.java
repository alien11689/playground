package dpr.playground.taskprovider.endpoint;

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
import dpr.playground.taskprovider.tasks.CreateTaskCommand;
import dpr.playground.taskprovider.tasks.UpdateTaskCommand;

@RestController
class TasksController implements TasksApi {
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    TasksController(TaskService taskService, TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    private LoggedUser getCurrentUser() {
        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public ResponseEntity<TaskDTO> addTask(AddTaskRequestDTO addTaskRequest) {
        var currentUser = getCurrentUser();
        var command = new CreateTaskCommand(
                addTaskRequest.getSummary(),
                addTaskRequest.getDescription(),
                currentUser.getId());
        var task = taskService.createTask(command);
        return new ResponseEntity<>(taskMapper.toDtoWithAssignee(task), HttpStatus.CREATED);
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
        var task = taskRepository.findById(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(taskMapper.toDtoWithAssignee(task));
    }

    @Override
    public ResponseEntity<GetTaskCommentsResponseDTO> getTaskComments(UUID taskId, Integer page, Integer size) {
        // TODO implement
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ResponseEntity<GetTasksResponseDTO> getTasks(Integer page, Integer size) {
        var pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        var tasksPage = taskRepository.findAll(pageable);
        return ResponseEntity.ok(taskMapper.toGetTasksResponse(tasksPage));
    }

    @Override
    public ResponseEntity<Void> updateTask(UUID taskId, TaskDTO task) {
        var currentUser = getCurrentUser();
        var existingTask = taskRepository.findById(taskId);
        if (existingTask == null) {
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
        // TODO implement
        throw new RuntimeException("Not implemented");
    }
}

