package dpr.playground.taskprovider;

import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import dpr.playground.taskprovider.tasks.model.UserDTO;
import dpr.playground.taskprovider.tasks.CommentRepository;
import dpr.playground.taskprovider.tasks.ProjectRepository;
import dpr.playground.taskprovider.tasks.TaskRepository;
import dpr.playground.taskprovider.user.UserRepository;
import dpr.playground.taskprovider.user.token.AccessTokenRepository;

abstract class AbstractIntegrationTest {

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
    protected TestRestTemplate restTemplate;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected AccessTokenRepository accessTokenRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected UserRepository userRepository;

    /**
     * Cleanup all data from the database to ensure test isolation.
     * Called as the first test in each integration test class using @Order(1).
     */
    protected void cleanupAllDatabaseTables() {
        // Delete in reverse order of foreign key dependencies
        commentRepository.deleteAll();
        accessTokenRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected UserDTO createUserSuccessfully(CreateUserDTO createUserDTO) throws URISyntaxException {
        ResponseEntity<UserDTO> createUserResponse = createUser(createUserDTO, UserDTO.class);
        Assertions.assertEquals(HttpStatus.CREATED, createUserResponse.getStatusCode());
        Assertions.assertNotNull(createUserResponse.getBody());
        return createUserResponse.getBody();
    }

    protected <T> ResponseEntity<T> createUser(CreateUserDTO createUserDTO, Class<T> responseType) throws URISyntaxException {
        return restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(createUserDTO), responseType);
    }

    protected LoginResponseDTO loginSuccessfully(String username, String password) throws URISyntaxException {
        HttpHeaders loginHeaders = createBasicAuthHeaders(username, password);
        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate.exchange("/login", HttpMethod.POST, new HttpEntity<>(loginHeaders), LoginResponseDTO.class);
        LoginResponseDTO loginResponseDTO = loginResponse.getBody();
        Assertions.assertNotNull(loginResponseDTO);
        return loginResponseDTO;
    }

    protected HttpHeaders createBasicAuthHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        String authValue = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        headers.set(HttpHeaders.AUTHORIZATION, authValue);
        return headers;
    }

    protected HttpHeaders createBearerAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return headers;
    }
}
