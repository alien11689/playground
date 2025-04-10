package dpr.playground.taskprovider;

import dpr.playground.taskprovider.tasks.model.GetTasksResponse;
import dpr.playground.taskprovider.tasks.model.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskProviderApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldAllowGettingTasksOnlyWithToken() throws URISyntaxException {
        MultiValueMap<String, String> loginHeaders = MultiValueMap.fromSingleValue(Map.of(HttpHeaders.AUTHORIZATION, "Basic " +Base64.getEncoder().encodeToString("user:password".getBytes())));
        var loginResponse = restTemplate.exchange(new RequestEntity<>(loginHeaders, HttpMethod.POST, new URI("http://localhost:" + port + "/login")), LoginResponse.class);
        assertNotNull(loginResponse.getBody());
        MultiValueMap<String, String> getTasksHeaders = MultiValueMap.fromSingleValue(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getBody().getToken()));
        var getTasksResponse = restTemplate.exchange(new RequestEntity<>(getTasksHeaders, HttpMethod.GET, new URI("http://localhost:" + port + "/tasks")), GetTasksResponse.class);
        assertEquals(HttpStatus.OK, getTasksResponse.getStatusCode());
        assertNotNull(getTasksResponse.getBody());
        assertTrue(getTasksResponse.getBody().getTasks().isEmpty());
    }

    @Test
    void shouldRejectGettingTasksWithUnknownToken() throws URISyntaxException {
        MultiValueMap<String, String> getTasksHeaders = MultiValueMap.fromSingleValue(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + UUID.randomUUID()));
        var getTasksResponse = restTemplate.exchange(new RequestEntity<>(getTasksHeaders, HttpMethod.GET, new URI("http://localhost:" + port + "/tasks")), GetTasksResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getTasksResponse.getStatusCode());
    }

}
