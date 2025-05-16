package dpr.playground.taskprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO;
import dpr.playground.taskprovider.tasks.model.GetUsersResponseDTO;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import dpr.playground.taskprovider.tasks.model.UserDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskProviderApplicationTests {

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

        var usersResponseDTO = getUsersSuccessfully(loginResponseDTO);
        assertTrue(usersResponseDTO.getUsers().contains(userDTO));
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
        var response = getUsers(headers);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private UserDTO createUserSuccessfully(CreateUserDTO createUserDTO) throws URISyntaxException {
        var createUserResponse = restTemplate.exchange(new RequestEntity<>(createUserDTO, HttpMethod.POST, new URI("/users")), UserDTO.class);
        assertEquals(HttpStatus.CREATED, createUserResponse.getStatusCode());
        assertNotNull(createUserResponse.getBody());
        return createUserResponse.getBody();
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
        assertTrue(tasks.getTasks().isEmpty());
    }

    private ResponseEntity<GetTasksResponseDTO> getTasks(MultiValueMap<String, String> bearerAuthHeaders) throws URISyntaxException {
        return restTemplate.exchange(new RequestEntity<>(bearerAuthHeaders, HttpMethod.GET, new URI("/tasks")), GetTasksResponseDTO.class);
    }

    private GetUsersResponseDTO getUsersSuccessfully(LoginResponseDTO loginResponseDTO) throws URISyntaxException {
        var bearerAuthHeaders = createBearerAuthHeaders(loginResponseDTO.getToken());
        var getUsersResponse = getUsers(bearerAuthHeaders);
        assertEquals(HttpStatus.OK, getUsersResponse.getStatusCode());
        var usersResponseDTO = getUsersResponse.getBody();
        assertNotNull(usersResponseDTO);
        return usersResponseDTO;
    }

    private ResponseEntity<GetUsersResponseDTO> getUsers(MultiValueMap<String, String> bearerAuthHeaders) throws URISyntaxException {
        return restTemplate.exchange(new RequestEntity<>(bearerAuthHeaders, HttpMethod.GET, new URI("/users")), GetUsersResponseDTO.class);
    }

    private MultiValueMap<String, String> createBasicAuthHeaders(String username, String password) {
        var authValue = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        return MultiValueMap.fromSingleValue(Map.of(HttpHeaders.AUTHORIZATION, authValue));
    }

    private MultiValueMap<String, String> createBearerAuthHeaders(String token) {
        return MultiValueMap.fromSingleValue(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }
}
