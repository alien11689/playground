package dpr.playground.taskprovider;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Order;
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
    @Order(1)
    void cleanupDatabase() {
        cleanupAllDatabaseTables();
    }

    @Test
    @Order(2)
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

        // First, create a comment
        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent(TestDataGenerator.CommentGenerator.randomCommentContent());
        ResponseEntity<CommentDTO> commentResponse = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addCommentRequest, createBearerAuthHeaders(loginResponse.getToken())),
                CommentDTO.class);
        var comment = commentResponse.getBody();

        // Then update the comment
        var updateCommentRequest = new CommentDTO();
        updateCommentRequest.setContent(TestDataGenerator.CommentGenerator.randomCommentContent());
        ResponseEntity<Void> response = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments/" + comment.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateCommentRequest, createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @Order(3)
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

        // First, create a comment
        var addCommentRequest = new AddTaskCommentRequestDTO();
        addCommentRequest.setContent(TestDataGenerator.CommentGenerator.randomCommentContent());
        ResponseEntity<CommentDTO> commentResponse = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addCommentRequest, createBearerAuthHeaders(loginResponse.getToken())),
                CommentDTO.class);
        var comment = commentResponse.getBody();

        // Archive the project
        restTemplate.exchange(
                "/projects/" + project.getId() + "?action=archive",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        // Try to update the comment - should fail because project is archived
        var updateCommentRequest = new CommentDTO();
        updateCommentRequest.setContent(TestDataGenerator.CommentGenerator.randomCommentContent());

        ResponseEntity<String> response = restTemplate.exchange(
                "/tasks/" + task.getId() + "/comments/" + comment.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateCommentRequest, createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
