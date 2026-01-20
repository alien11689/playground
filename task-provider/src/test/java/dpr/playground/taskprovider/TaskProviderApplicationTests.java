package dpr.playground.taskprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequestDTO;
import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.CommentDTO;
import dpr.playground.taskprovider.tasks.model.CreateProjectRequestDTO;
import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.ErrorDTO;
import dpr.playground.taskprovider.tasks.model.GetTaskCommentsResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetUsersResponseDTO;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import dpr.playground.taskprovider.tasks.model.ProjectDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;
import dpr.playground.taskprovider.tasks.model.TaskStatusDTO;
import dpr.playground.taskprovider.tasks.model.UserDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskProviderApplicationTests extends AbstractIntegrationTest {
    private LoginResponseDTO loggedInUser;

    @BeforeEach
    void setupLoggedInUser() throws URISyntaxException {
        var createUserDTO = new CreateUserDTO(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        createUserSuccessfully(createUserDTO);
        loggedInUser = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());
    }

    @Test
    @Order(1)
    void shouldReturnConflictWhenUsernameAlreadyExists() throws URISyntaxException {
        var createUserDTO = new CreateUserDTO(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        createUserSuccessfully(createUserDTO);

        ResponseEntity<ErrorDTO> errorResponse = createUser(createUserDTO, ErrorDTO.class);
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    @Order(3)
    void shouldRejectGettingTasksWithUnknownToken() throws URISyntaxException {
        var headers = createBearerAuthHeaders(UUID.randomUUID().toString());
        var getTasksResponse = getTasks(headers);
        assertEquals(HttpStatus.UNAUTHORIZED, getTasksResponse.getStatusCode());
    }

    @Test
    @Order(4)
    void shouldRejectGettingUsersWithUnknownToken() throws URISyntaxException {
        var headers = createBearerAuthHeaders(UUID.randomUUID().toString());
        var response = getUsers(headers, null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(5)
    void shouldRejectGettingTaskWithUnknownToken() throws URISyntaxException {
        var headers = createBearerAuthHeaders(UUID.randomUUID().toString());
        var taskId = UUID.randomUUID();

        var response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.GET, new HttpEntity<>(headers), ErrorDTO.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(6)
    void shouldRejectUpdatingTaskWithUnknownToken() throws URISyntaxException {
        var headers = createBearerAuthHeaders(UUID.randomUUID().toString());
        var taskId = UUID.randomUUID();

        var updateRequest = new TaskDTO();
        updateRequest.setSummary("Updated summary");

        var response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(8)
    void shouldAllowGettingTasksOnlyWithToken() throws URISyntaxException {
        var createUserDTO = new CreateUserDTO(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        var userDTO = createUserSuccessfully(createUserDTO);
        var loginResponseDTO = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        getTasksSuccessfully(loginResponseDTO);

        var users = getAllUsersSuccessfully(loginResponseDTO);
        assertTrue(users.contains(userDTO));
    }

    @Test
    @Order(9)
    void shouldReturnBadRequestWhenCreatingTaskWithEmptySummary() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary("");
        addTaskRequest.setDescription("Valid description");

        var response = restTemplate.exchange("/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(10)
    void shouldReturnBadRequestWhenCreatingTaskWithNullSummary() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setDescription("Valid description");

        var response = restTemplate.exchange("/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private UUID createProject() throws URISyntaxException {
        var createProjectRequest = new CreateProjectRequestDTO();
        createProjectRequest.setName("Test Project " + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        createProjectRequest.setDescription("Test Description " + UUID.randomUUID().toString().replace("-", "").substring(0, 8));

        var projectResponse = restTemplate.exchange("/projects", HttpMethod.POST, new HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loggedInUser.getToken())), ProjectDTO.class);
        return projectResponse.getBody().getId();
    }

    private UUID createTaskWithProjectId(HttpHeaders headers, String summary, String description, UUID projectId) throws URISyntaxException {
        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary(summary);
        addTaskRequest.setDescription(description);
        if (projectId != null) {
            addTaskRequest.setProjectId(projectId);
        }

        var response = restTemplate.exchange("/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, headers), TaskDTO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().getId();
    }

    private UUID createTask() throws URISyntaxException {
        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary("Task summary");
        addTaskRequest.setDescription("Task description");

        var response = restTemplate.exchange("/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loggedInUser.getToken())), TaskDTO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().getId();
    }

    private void getTasksSuccessfully(LoginResponseDTO loginResponseDTO) throws URISyntaxException {
        var headers = createBearerAuthHeaders(loginResponseDTO.getToken());
        var getTasksResponse = getTasks(headers);
        assertEquals(HttpStatus.OK, getTasksResponse.getStatusCode());
        var tasks = getTasksResponse.getBody();
        assertNotNull(tasks);
    }

    private ResponseEntity<GetTasksResponseDTO> getTasks(HttpHeaders headers) throws URISyntaxException {
        return restTemplate.exchange("/tasks", HttpMethod.GET, new HttpEntity<>(headers), GetTasksResponseDTO.class);
    }

    private List<UserDTO> getAllUsersSuccessfully(LoginResponseDTO loginResponseDTO) throws URISyntaxException {
        var headers = createBearerAuthHeaders(loginResponseDTO.getToken());
        var page = 0;
        var size = 10;
        var result = new ArrayList<UserDTO>();
        while (true) {
            var getUsersResponse = getUsers(headers, page, size);
            assertEquals(HttpStatus.OK, getUsersResponse.getStatusCode());
            var usersResponseDTO = getUsersResponse.getBody();
            assertNotNull(usersResponseDTO);
            result.addAll(usersResponseDTO.getContent());
            page++;
            if (usersResponseDTO.getLast()) {
                break;
            }
        }
        return result;
    }

    private ResponseEntity<GetUsersResponseDTO> getUsers(HttpHeaders headers, Integer page, Integer size) throws URISyntaxException {
        var uriBuilder = new StringBuilder("/users");
        if (page != null || size != null) {
            uriBuilder.append("?");
            if (page != null) {
                uriBuilder.append("page=").append(page);
            }
            if (size != null) {
                if (page != null) {
                    uriBuilder.append("&");
                }
                uriBuilder.append("size=").append(size);
            }
        }
        return restTemplate.exchange(uriBuilder.toString(), HttpMethod.GET, new HttpEntity<>(headers), GetUsersResponseDTO.class);
    }

    @Test
    @Order(0)
    void shouldReturnEmptyListWhenNoTasksExist() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var getTasksResponse = restTemplate.exchange("/tasks", HttpMethod.GET, new HttpEntity<>(headers), GetTasksResponseDTO.class);
        assertEquals(HttpStatus.OK, getTasksResponse.getStatusCode());
        assertNotNull(getTasksResponse.getBody());
        var content = getTasksResponse.getBody().getContent();
        assertTrue(content == null || content.isEmpty(), "Expected empty or null content list, but got: " + content);
        assertEquals(0, getTasksResponse.getBody().getTotalElements());
    }

    @Test
    @Order(10)
    void shouldCreateTaskSuccessfully() throws URISyntaxException {
        var projectId = createProject();

        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary("Test task summary");
        addTaskRequest.setDescription("Test task description");
        addTaskRequest.setProjectId(projectId);

        var response = restTemplate.exchange("/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, headers), TaskDTO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test task summary", response.getBody().getSummary());
        assertEquals("Test task description", response.getBody().getDescription());
        assertEquals(TaskStatusDTO.NEW, response.getBody().getStatus());
        assertNotNull(response.getBody().getId());
        assertNotNull(response.getBody().getCreatedAt());
        assertNotNull(response.getBody().getCreatedBy());
    }

    @Test
    @Order(11)
    void shouldGetExistingTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", null);

        var response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.GET, new HttpEntity<>(headers), TaskDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Task summary", response.getBody().getSummary());
        assertEquals("Task description", response.getBody().getDescription());
        assertEquals(TaskStatusDTO.NEW, response.getBody().getStatus());
        assertEquals(taskId, response.getBody().getId());
    }

    @Test
    @Order(12)
    void shouldReturnNotFoundWhenGettingNonExistentTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = UUID.randomUUID();

        var response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.GET, new HttpEntity<>(headers), ErrorDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(13)
    void shouldUpdateTaskSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId = createTaskWithProjectId(headers, "Original summary", "Original description", projectId);

        var response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.GET, new HttpEntity<>(headers), TaskDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Original summary", response.getBody().getSummary());
        assertEquals("Original description", response.getBody().getDescription());
        assertEquals(TaskStatusDTO.NEW, response.getBody().getStatus());
        assertEquals(taskId, response.getBody().getId());

        var updateRequest = new TaskDTO();
        updateRequest.setSummary("Updated summary");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(TaskStatusDTO.PENDING);

        var updateResponse = restTemplate.exchange("/tasks/" + taskId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, updateResponse.getStatusCode());

        var updatedTaskResponse = restTemplate.exchange("/tasks/" + taskId, HttpMethod.GET, new HttpEntity<>(headers), TaskDTO.class);
        assertEquals(HttpStatus.OK, updatedTaskResponse.getStatusCode());
        assertEquals("Updated summary", updatedTaskResponse.getBody().getSummary());
        assertEquals("Updated description", updatedTaskResponse.getBody().getDescription());
        assertEquals(TaskStatusDTO.PENDING, updatedTaskResponse.getBody().getStatus());
        assertEquals(taskId, updatedTaskResponse.getBody().getId());
    }

    @Test
    @Order(14)
    void shouldReturnNotFoundWhenUpdatingNonExistentTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = UUID.randomUUID();

        var updateRequest = new TaskDTO();
        updateRequest.setSummary("Updated summary");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(TaskStatusDTO.PENDING);

        var response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(15)
    void shouldReturnBadRequestWhenAddingCommentWithEmptyContent() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", projectId);

        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("");

        ResponseEntity<String> response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(16)
    void shouldReturnBadRequestWhenAddingCommentWithWhitespaceContent() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", projectId);

        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("   ");

        ResponseEntity<String> response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(17)
    void shouldReturnNotFoundWhenAddingCommentToNonExistentTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = UUID.randomUUID();

        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Comment content");

        ResponseEntity<String> response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(18)
    void shouldReturnNotFoundWhenAddingCommentToClosedTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", null);

        ResponseEntity<String> response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.PUT, new HttpEntity<>(new TaskDTO(), headers), String.class);

        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Comment content");

        ResponseEntity<String> addCommentResponse = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, addCommentResponse.getStatusCode());
    }

    @Test
    @Order(19)
    void shouldGetCommentsEmptyListWhenNoCommentsExist() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", projectId);

        var getCommentsResponse = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.GET, new HttpEntity<>(headers), GetTaskCommentsResponseDTO.class);
        assertEquals(HttpStatus.OK, getCommentsResponse.getStatusCode());
        var comments = getCommentsResponse.getBody();
        assertEquals(0, comments.size());
    }

    @Test
    @Order(20)
    void shouldGetCommentsSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", projectId);

        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Comment content");

        ResponseEntity<String> response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(21)
    void shouldGetCommentsSortedByNewestFirst() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId1 = createTaskWithProjectId(headers, "Task 1", "Task 1 description", projectId);
        var taskId2 = createTaskWithProjectId(headers, "Task 2", "Task 2 description", projectId);
        var taskId3 = createTaskWithProjectId(headers, "Task 3", "Task 3 description", projectId);

        var addCommentRequest1 = new AddTaskCommentRequestDTO();
        addCommentRequest1.setContent("Comment 1");
        var addCommentRequest2 = new AddTaskCommentRequestDTO();
        addCommentRequest2.setContent("Comment 2");
        var addCommentRequest3 = new AddTaskCommentRequestDTO();
        addCommentRequest3.setContent("Comment 3");

        ResponseEntity<String> response1 = restTemplate.exchange("/tasks/" + taskId1 + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest1, headers), String.class);
        ResponseEntity<String> response2 = restTemplate.exchange("/tasks/" + taskId2 + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest2, headers), String.class);
        ResponseEntity<String> response3 = restTemplate.exchange("/tasks/" + taskId3 + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest3, headers), String.class);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        assertNotNull(response1.getBody());
        assertNotNull(response2.getBody());
        assertNotNull(response3.getBody());
    }

    @Test
    @Order(22)
    void shouldReturnNotFoundWhenGettingCommentsForNonExistentTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = UUID.randomUUID();

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.GET, new HttpEntity<>(headers), GetTaskCommentsResponseDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(23)
    void shouldUpdateCommentSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", projectId);

        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Original comment");

        ResponseEntity<String> response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(24)
    void shouldDeleteCommentSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", projectId);

        ResponseEntity<String> createCommentResponse = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(new AddTaskCommentRequestDTO(), headers), String.class);
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + createCommentResponse.getBody().getId(), HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @Order(25)
    void shouldReturnForbiddenWhenDeletingAnotherUsersComment() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var projectId = createProject();
        var taskId = createTaskWithProjectId(headers, "Task summary", "Task description", projectId);

        ResponseEntity<String> createCommentResponse = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(new AddTaskCommentRequestDTO(), headers), String.class);
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + createCommentResponse.getBody().getId(), HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Order(26)
    void shouldReturnNotFoundWhenDeletingNonExistentComment() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = UUID.randomUUID();

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + UUID.randomUUID(), HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(27)
    void shouldReturnNotFoundWhenUpdatingNonExistentComment() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = UUID.randomUUID();

        var updateRequest = new CommentDTO();
        updateRequest.setContent("Updated comment");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + UUID.randomUUID(), HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
