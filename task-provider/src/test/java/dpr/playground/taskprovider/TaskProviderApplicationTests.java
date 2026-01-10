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

import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.ErrorDTO;
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

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:17-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
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
if (loggedInUser == null){
    var createUserDTO = new CreateUserDTO(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        createUserSuccessfully(createUserDTO);
        loggedInUser = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());
    }}

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
}
