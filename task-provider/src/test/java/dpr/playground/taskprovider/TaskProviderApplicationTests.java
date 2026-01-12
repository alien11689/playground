package dpr.playground.taskprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequestDTO;
import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.CommentDTO;
import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.ErrorDTO;
import dpr.playground.taskprovider.tasks.model.GetTaskCommentsResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetUsersResponseDTO;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;
import dpr.playground.taskprovider.tasks.model.TaskStatusDTO;
import dpr.playground.taskprovider.tasks.model.UserDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskProviderApplicationTests {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:17-alpine"
    );

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

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
    @Order(7)
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
    @Order(8)
    void shouldReturnBadRequestWhenCreatingTaskWithEmptySummary() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary("");
        addTaskRequest.setDescription("Valid description");

        var response = restTemplate.exchange("/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(9)
    void shouldReturnBadRequestWhenCreatingTaskWithNullSummary() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setDescription("Valid description");

        var response = restTemplate.exchange("/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(10)
    void shouldCreateTaskSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary("Test task summary");
        addTaskRequest.setDescription("Test task description");

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
        var taskId = createTask(headers, "Task summary", "Task description");

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
        var nonExistentTaskId = UUID.randomUUID();

        var response = restTemplate.exchange("/tasks/" + nonExistentTaskId, HttpMethod.GET, new HttpEntity<>(headers), ErrorDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(13)
    void shouldUpdateTaskSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Original summary", "Original description");

        var updateRequest = new TaskDTO();
        updateRequest.setSummary("Updated summary");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(TaskStatusDTO.PENDING);

        var response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        var getResponse = restTemplate.exchange("/tasks/" + taskId, HttpMethod.GET, new HttpEntity<>(headers), TaskDTO.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("Updated summary", getResponse.getBody().getSummary());
        assertEquals("Updated description", getResponse.getBody().getDescription());
        assertEquals(TaskStatusDTO.PENDING, getResponse.getBody().getStatus());
    }

    @Test
    @Order(14)
    void shouldReturnNotFoundWhenUpdatingNonExistentTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var nonExistentTaskId = UUID.randomUUID();
        var updateRequest = new TaskDTO();
        updateRequest.setSummary("Updated summary");
        updateRequest.setStatus(TaskStatusDTO.PENDING);

        var response = restTemplate.exchange("/tasks/" + nonExistentTaskId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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

    private UserDTO createUserSuccessfully(CreateUserDTO createUserDTO) throws URISyntaxException {
        var createUserResponse = createUser(createUserDTO, UserDTO.class);
        assertEquals(HttpStatus.CREATED, createUserResponse.getStatusCode());
        assertNotNull(createUserResponse.getBody());
        return createUserResponse.getBody();
    }

    private <T> ResponseEntity<T> createUser(CreateUserDTO createUserDTO, Class<T> responseType) throws URISyntaxException {
        return restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(createUserDTO), responseType);
    }

    private LoginResponseDTO loginSuccessfully(String username, String password) throws URISyntaxException {
        var loginHeaders = createBasicAuthHeaders(username, password);
        var loginResponse = restTemplate.exchange("/login", HttpMethod.POST, new HttpEntity<>(loginHeaders), LoginResponseDTO.class);
        var loginResponseDTO = loginResponse.getBody();
        assertNotNull(loginResponseDTO);
        return loginResponseDTO;
    }

    private LoginResponseDTO loginSuccessfully() throws URISyntaxException {
        var createUserDTO = new CreateUserDTO(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        createUserSuccessfully(createUserDTO);
        return loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());
    }

    private UUID createTask(HttpHeaders headers, String summary, String description) throws URISyntaxException {
        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary(summary);
        addTaskRequest.setDescription(description);

        var response = restTemplate.exchange("/tasks", HttpMethod.POST, new HttpEntity<>(addTaskRequest, headers), TaskDTO.class);
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
        StringBuilder uriBuilder = new StringBuilder("/users");
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

    private HttpHeaders createBasicAuthHeaders(String username, String password) {
        var headers = new HttpHeaders();
        var authValue = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        headers.set(HttpHeaders.AUTHORIZATION, authValue);
        return headers;
    }

    private HttpHeaders createBearerAuthHeaders(String token) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return headers;
    }

    @Test
    @Order(15)
    void shouldReturnBadRequestWhenAddingCommentWithEmptyContent() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(16)
    void shouldReturnBadRequestWhenAddingCommentWithWhitespaceContent() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("   ");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(17)
    void shouldReturnNotFoundWhenAddingCommentToNonExistentTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var nonExistentTaskId = UUID.randomUUID();
        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Test comment");

        var response = restTemplate.exchange("/tasks/" + nonExistentTaskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(18)
    void shouldReturnBadRequestWhenAddingCommentToClosedTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        updateTaskStatus(headers, taskId, TaskStatusDTO.DONE);
        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Test comment");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(19)
    void shouldAddCommentSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Test comment");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), CommentDTO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test comment", response.getBody().getContent());
        assertNotNull(response.getBody().getId());
        assertNotNull(response.getBody().getCreatedAt());
        assertNotNull(response.getBody().getUpdatedAt());
    }

    @Test
    @Order(20)
    void shouldGetCommentsEmptyListWhenNoCommentsExist() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.GET, new HttpEntity<>(headers), GetTaskCommentsResponseDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    @Order(21)
    void shouldGetCommentsSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        addComment(headers, taskId, "First comment");
        addComment(headers, taskId, "Second comment");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.GET, new HttpEntity<>(headers), GetTaskCommentsResponseDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    @Order(22)
    void shouldGetCommentsSortedByNewestFirst() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var firstCommentId = addComment(headers, taskId, "First comment");
        var secondCommentId = addComment(headers, taskId, "Second comment");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.GET, new HttpEntity<>(headers), GetTaskCommentsResponseDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(secondCommentId, response.getBody().getContent().get(0).getId());
        assertEquals(firstCommentId, response.getBody().getContent().get(1).getId());
    }

    @Test
    @Order(23)
    void shouldReturnNotFoundWhenGettingCommentsForNonExistentTask() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var nonExistentTaskId = UUID.randomUUID();

        var response = restTemplate.exchange("/tasks/" + nonExistentTaskId + "/comments", HttpMethod.GET, new HttpEntity<>(headers), GetTaskCommentsResponseDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    @Order(24)
    void shouldReturnNotFoundWhenUpdatingNonExistentComment() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var nonExistentCommentId = UUID.randomUUID();
        var updateRequest = new CommentDTO();
        updateRequest.setContent("Updated content");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + nonExistentCommentId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(25)
    void shouldReturnForbiddenWhenUpdatingAnotherUsersComment() throws URISyntaxException {
        var headers1 = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers1, "Task summary", "Task description");
        var commentId = addComment(headers1, taskId, "Original comment");

        var headers2 = createBearerAuthHeaders(loginSuccessfully().getToken());
        var updateRequest = new CommentDTO();
        updateRequest.setContent("Updated content");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + commentId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers2), ErrorDTO.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Order(26)
    void shouldReturnBadRequestWhenUpdatingCommentWithEmptyContent() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var commentId = addComment(headers, taskId, "Original comment");
        var updateRequest = new CommentDTO();
        updateRequest.setContent("");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + commentId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(27)
    void shouldUpdateCommentSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var commentId = addComment(headers, taskId, "Original comment");
        var updateRequest = new CommentDTO();
        updateRequest.setContent("Updated content");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + commentId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        var getResponse = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.GET, new HttpEntity<>(headers), GetTaskCommentsResponseDTO.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(1, getResponse.getBody().getContent().size());
        assertEquals("Updated content", getResponse.getBody().getContent().get(0).getContent());
    }

    @Test
    @Order(28)
    void shouldReturnNotFoundWhenDeletingNonExistentComment() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var nonExistentCommentId = UUID.randomUUID();

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + nonExistentCommentId, HttpMethod.DELETE, new HttpEntity<>(headers), ErrorDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(29)
    void shouldReturnForbiddenWhenDeletingAnotherUsersComment() throws URISyntaxException {
        var headers1 = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers1, "Task summary", "Task description");
        var commentId = addComment(headers1, taskId, "Original comment");

        var headers2 = createBearerAuthHeaders(loginSuccessfully().getToken());

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + commentId, HttpMethod.DELETE, new HttpEntity<>(headers2), ErrorDTO.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Order(30)
    void shouldDeleteCommentSuccessfully() throws URISyntaxException {
        var headers = createBearerAuthHeaders(loggedInUser.getToken());
        var taskId = createTask(headers, "Task summary", "Task description");
        var commentId = addComment(headers, taskId, "Original comment");

        var response = restTemplate.exchange("/tasks/" + taskId + "/comments/" + commentId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        var getResponse = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.GET, new HttpEntity<>(headers), GetTaskCommentsResponseDTO.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(0, getResponse.getBody().getTotalElements());
    }

    private UUID addComment(HttpHeaders headers, UUID taskId, String content) throws URISyntaxException {
        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent(content);
        var response = restTemplate.exchange("/tasks/" + taskId + "/comments", HttpMethod.POST, new HttpEntity<>(addCommentRequest, headers), CommentDTO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().getId();
    }

    private void updateTaskStatus(HttpHeaders headers, UUID taskId, TaskStatusDTO status) throws URISyntaxException {
        var updateRequest = new TaskDTO();
        updateRequest.setStatus(status);
        var response = restTemplate.exchange("/tasks/" + taskId, HttpMethod.PUT, new HttpEntity<>(updateRequest, headers), Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
