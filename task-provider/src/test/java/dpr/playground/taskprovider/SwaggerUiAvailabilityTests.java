package dpr.playground.taskprovider;

import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dpr.playground.taskprovider.tasks.model.CreateUserDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerUiAvailabilityTests extends AbstractIntegrationTest {

    private String authToken;

    @BeforeEach
    void setupLoggedInUser() throws URISyntaxException {
        CreateUserDTO createUserDTO = new CreateUserDTO(
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
        ResponseEntity<String> response = restTemplate.exchange("/swagger-ui.html", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().contains("Swagger UI"));
    }

    @Test
    void shouldServeOpenApiSpec() {
        var headers = createBearerAuthHeaders(authToken);
        ResponseEntity<String> response = restTemplate.exchange("/v3/api-docs", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody().contains("openapi"));
    }
}
