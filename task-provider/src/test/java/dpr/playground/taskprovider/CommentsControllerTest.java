package dpr.playground.taskprovider;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequestDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;
import dpr.playground.taskprovider.tasks.model.CommentDTO;
import dpr.playground.taskprovider.TestDataGenerator;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentsControllerTest extends AbstractIntegrationTest {
    @Test
    void updateComment_withActiveProject_shouldReturn204() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
        ResponseEntity<dpr.playground.taskprovider.tasks.model.ProjectDTO> projectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.ProjectDTO.class);
        var project = projectResponse.getBody();

        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(project.getId());

        ResponseEntity<TaskDTO> taskResponse = restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                TaskDTO.class);
        var task = taskResponse.getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments/" + task.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(TestDataGenerator.CommentGenerator.randomCommentRequestDTO(), createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void updateComment_withArchivedProject_shouldReturn400() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
        ResponseEntity<dpr.playground.taskprovider.tasks.model.ProjectDTO> projectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.ProjectDTO.class);
        var project = projectResponse.getBody();

        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(project.getId());

        ResponseEntity<TaskDTO> taskResponse = restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                TaskDTO.class);
        var task = taskResponse.getBody();

        restTemplate.exchange(
                "/projects/" + project.getId() + "?action=archive",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        var updateRequest = new CommentDTO();
        updateRequest.setContent(TestDataGenerator.CommentGenerator.randomCommentContent());

        ResponseEntity<String> response = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments/" + task.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateRequest, createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
