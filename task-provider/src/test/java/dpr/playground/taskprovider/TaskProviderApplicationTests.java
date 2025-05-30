package dpr.playground.taskprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;

import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.ErrorDTO;
import dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetUsersResponseDTO;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import dpr.playground.taskprovider.tasks.model.UserDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskProviderApplicationTests {

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
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

    @Test
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
    void shouldRejectGettingTasksWithUnknownToken() throws URISyntaxException {
        var headers = createBearerAuthHeaders(UUID.randomUUID().toString());
        var getTasksResponse = getTasks(headers);
        assertEquals(HttpStatus.UNAUTHORIZED, getTasksResponse.getStatusCode());
    }

    @Test
    void shouldRejectGettingUsersWithUnknownToken() throws URISyntaxException {
        var headers = createBearerAuthHeaders(UUID.randomUUID().toString());
        var response = getUsers(headers, null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private UserDTO createUserSuccessfully(CreateUserDTO createUserDTO) throws URISyntaxException {
        var createUserResponse = createUser(createUserDTO, UserDTO.class);
        assertEquals(HttpStatus.CREATED, createUserResponse.getStatusCode());
        assertNotNull(createUserResponse.getBody());
        return createUserResponse.getBody();
    }

    private <T> ResponseEntity<T> createUser(CreateUserDTO createUserDTO, Class<T> responseType) throws URISyntaxException {
        return restTemplate.exchange(new RequestEntity<>(createUserDTO, HttpMethod.POST, new URI("/users")), responseType);
    }

    private LoginResponseDTO loginSuccessfully(String username, String password) throws URISyntaxException {
        var loginHeaders = createBasicAuthHeaders(username, password);
        var loginResponse = restTemplate.exchange(new RequestEntity<>(loginHeaders, HttpMethod.POST, new URI("/login")), LoginResponseDTO.class);
        var loginResponseDTO = loginResponse.getBody();
        assertNotNull(loginResponseDTO);
        return loginResponseDTO;
    }

    private void getTasksSuccessfully(LoginResponseDTO loginResponseDTO) throws URISyntaxException {
        var bearerAuthHeaders = createBearerAuthHeaders(loginResponseDTO.getToken());
        var getTasksResponse = getTasks(bearerAuthHeaders);
        assertEquals(HttpStatus.OK, getTasksResponse.getStatusCode());
        var tasks = getTasksResponse.getBody();
        assertNotNull(tasks);
        assertTrue(tasks.getContent().isEmpty());
    }

    private ResponseEntity<GetTasksResponseDTO> getTasks(MultiValueMap<String, String> bearerAuthHeaders) throws URISyntaxException {
        return restTemplate.exchange(new RequestEntity<>(bearerAuthHeaders, HttpMethod.GET, new URI("/tasks")), GetTasksResponseDTO.class);
    }

    private List<UserDTO> getAllUsersSuccessfully(LoginResponseDTO loginResponseDTO) throws URISyntaxException {
        var bearerAuthHeaders = createBearerAuthHeaders(loginResponseDTO.getToken());
        var page = 0;
        var size = 10;
        var result = new ArrayList<UserDTO>();
        while (true) {
            var getUsersResponse = getUsers(bearerAuthHeaders, page, size);
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

    private ResponseEntity<GetUsersResponseDTO> getUsers(MultiValueMap<String, String> bearerAuthHeaders, Integer page, Integer size) throws URISyntaxException {
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
        return restTemplate.exchange(new RequestEntity<>(bearerAuthHeaders, HttpMethod.GET, new URI(uriBuilder.toString())), GetUsersResponseDTO.class);
    }

    private MultiValueMap<String, String> createBasicAuthHeaders(String username, String password) {
        var authValue = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        return MultiValueMap.fromSingleValue(Map.of(HttpHeaders.AUTHORIZATION, authValue));
    }

    private MultiValueMap<String, String> createBearerAuthHeaders(String token) {
        return MultiValueMap.fromSingleValue(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }
}
