package dpr.playground.taskprovider;

import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import dpr.playground.taskprovider.tasks.model.UserDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerUiAvailabilityTests extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;

    @BeforeEach
    void setupLoggedInUser() throws URISyntaxException {
        var createUserDTO = new CreateUserDTO(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        createUserSuccessfully(createUserDTO);
        authToken = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword()).getToken();
    }

    @Test
    void shouldServeSwaggerUiHtml() {
        var headers = createBearerAuthHeaders(authToken);
        ResponseEntity<String> response = restTemplate.exchange("/swagger-ui.html", org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), String.class);
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getBody());
        org.junit.jupiter.api.Assertions.assertTrue(response.getBody().contains("Swagger UI"));
    }

    @Test
    void shouldServeOpenApiSpec() {
        var headers = createBearerAuthHeaders(authToken);
        ResponseEntity<String> response = restTemplate.exchange("/v3/api-docs", org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), String.class);
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getBody());
        org.junit.jupiter.api.Assertions.assertTrue(response.getBody().contains("openapi"));
    }

    private UserDTO createUserSuccessfully(CreateUserDTO createUserDTO) throws URISyntaxException {
        var createUserResponse = createUser(createUserDTO, UserDTO.class);
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.CREATED, createUserResponse.getStatusCode());
        org.junit.jupiter.api.Assertions.assertNotNull(createUserResponse.getBody());
        return createUserResponse.getBody();
    }

    private <T> ResponseEntity<T> createUser(CreateUserDTO createUserDTO, Class<T> responseType) throws URISyntaxException {
        return restTemplate.exchange("/users", org.springframework.http.HttpMethod.POST, new HttpEntity<>(createUserDTO), responseType);
    }

    private LoginResponseDTO loginSuccessfully(String username, String password) throws URISyntaxException {
        var loginHeaders = createBasicAuthHeaders(username, password);
        var loginResponse = restTemplate.exchange("/login", org.springframework.http.HttpMethod.POST, new HttpEntity<>(loginHeaders), LoginResponseDTO.class);
        var loginResponseDTO = loginResponse.getBody();
        org.junit.jupiter.api.Assertions.assertNotNull(loginResponseDTO);
        return loginResponseDTO;
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
