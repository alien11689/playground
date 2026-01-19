package dpr.playground.taskprovider;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dpr.playground.taskprovider.tasks.model.AddTaskCommentRequestDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentsControllerTest extends AbstractIntegrationTest {
    @Test
    void updateComment_withActiveProject_shouldReturn204() throws URISyntaxException {
        var createUserDTO = new dpr.playground.taskprovider.tasks.model.CreateUserDTO("testuser", "testpass", "Test", "User");
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername()), "testpass");

        var createProjectRequest = new dpr.playground.taskprovider.tasks.model.CreateProjectRequestDTO();
        createProjectRequest.setName("Test Project");
        var projectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest2, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.ProjectDTO.class);
        var project = projectResponse.getBody();

        var addTaskRequest = new dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(project.getId());
        var taskResponse = restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                TaskDTO.class);
        var task = taskResponse.getBody();

        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Comment: ");
        var commentResponse = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addCommentRequest, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.CommentDTO.class);
        var comment = commentResponse.getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments/" + comment.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>("Updated comment", createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void updateComment_withArchivedProject_shouldReturn400() throws URISyntaxException {
        var createUserDTO = new dpr.playground.taskprovider.tasks.model.CreateUserDTO("testuser", "testpass", "Test", "User");
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername()), "testpass");

        var createProjectRequest = new dpr.playground.taskprovider.tasks.model.CreateProjectRequestDTO();
        createProjectRequest.setName("Test Project");
        var projectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest2, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.ProjectDTO.class);
        var project = projectResponse.getBody();

        var addTaskRequest = new dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(project.getId());
        var taskResponse = restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                TaskDTO.class);
        var task = taskResponse.getBody();

        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent("Comment: ");
        var commentResponse = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addCommentRequest, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.CommentDTO.class);
        var comment = commentResponse.getBody();

        restTemplate.exchange(
                "/projects/" + project.getId() + "?action=archive",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments/" + comment.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>("Updated comment", createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
