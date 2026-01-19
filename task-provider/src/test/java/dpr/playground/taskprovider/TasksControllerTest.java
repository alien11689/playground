package dpr.playground.taskprovider;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO;
import dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO;
import dpr.playground.taskprovider.tasks.model.TaskDTO;
import dpr.playground.taskprovider.TestDataGenerator;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TasksControllerTest extends AbstractIntegrationTest {
    @Test
    void addTask_withValidProjectId_shouldReturn201() throws URISyntaxException {
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

        ResponseEntity<TaskDTO> response = restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                TaskDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(project.getId(), response.getBody().getProjectId());
    }

    @Test
    void addTask_withArchivedProject_shouldReturn400() throws URISyntaxException {
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

        restTemplate.exchange(
                "/projects/" + project.getId() + "?action=archive",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(project.getId());

        ResponseEntity<String> response = restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void addTask_withNonExistentProjectId_shouldReturn400() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(java.util.UUID.randomUUID());

        ResponseEntity<String> response = restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getTasks_withProjectId_shouldReturnFilteredTasks() throws URISyntaxException {
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

        var createProjectRequest2 = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
        ResponseEntity<dpr.playground.taskprovider.tasks.model.ProjectDTO> projectResponse2 = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest2, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.ProjectDTO.class);
        var project2 = projectResponse2.getBody();

        var addTaskRequest = new AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(project.getId());
        restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                TaskDTO.class);

        var addTaskRequest2 = new AddTaskRequestDTO();
        addTaskRequest2.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest2.setProjectId(project2.getId());
        restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest2, createBearerAuthHeaders(loginResponse.getToken())),
                TaskDTO.class);

        ResponseEntity<GetTasksResponseDTO> response = restTemplate.exchange(
                "/tasks?projectId=" + project.getId(),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                GetTasksResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getContent().size());
        assertTrue(response.getBody().getContent().stream().allMatch(t -> 
                project.getId().equals(t.getProjectId())));
    }

    @Test
    void updateTask_withActiveProject_shouldReturn204() throws URISyntaxException {
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

        var updateRequest = new TaskDTO();
        updateRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        updateRequest.setStatus(dpr.playground.taskprovider.tasks.model.TaskStatusDTO.PENDING);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/tasks/" + task.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateRequest, createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void updateTask_withArchivedProject_shouldReturn400() throws URISyntaxException {
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

        var updateRequest = new TaskDTO();
        updateRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        updateRequest.setStatus(dpr.playground.taskprovider.tasks.model.TaskStatusDTO.PENDING);

        ResponseEntity<String> response = restTemplate.exchange(
                "/tasks/" + task.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateRequest, createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
