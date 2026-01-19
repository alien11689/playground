package dpr.playground.taskprovider;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dpr.playground.taskprovider.tasks.model.CreateProjectRequestDTO;
import dpr.playground.taskprovider.tasks.model.GetProjectsResponseDTO;
import dpr.playground.taskprovider.tasks.model.ProjectDTO;
import dpr.playground.taskprovider.tasks.model.UpdateProjectRequestDTO;
import dpr.playground.taskprovider.TestDataGenerator;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectsControllerTest extends AbstractIntegrationTest {
    @Test
    void createProject_shouldReturn201WithValidData() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();

        ResponseEntity<ProjectDTO> response = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                ProjectDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getName());
        assertNotNull(response.getBody().getId());
        assertEquals(dpr.playground.taskprovider.tasks.model.ProjectStatusDTO.ACTIVE, response.getBody().getStatus());
    }

    @Test
    void createProject_shouldReturn400WithoutName() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = new CreateProjectRequestDTO();
        createProjectRequest.setDescription(TestDataGenerator.ProjectGenerator.randomProjectDescription());

        ResponseEntity<String> response = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getProject_shouldReturn200ForExistingProject() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();

        ResponseEntity<ProjectDTO> createdProjectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                ProjectDTO.class);
        var createdProject = createdProjectResponse.getBody();

        ResponseEntity<ProjectDTO> response = restTemplate.exchange(
                "/projects/" + createdProject.getId(),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                ProjectDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdProject.getId(), response.getBody().getId());
    }

    @Test
    void getProject_shouldReturn404ForNonExistentProject() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        ResponseEntity<String> response = restTemplate.exchange(
                "/projects/" + java.util.UUID.randomUUID(),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProjects_shouldReturn200WithPagination() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        for (int i = 0; i < 5; i++) {
            var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
            restTemplate.exchange(
                    "/projects",
                    org.springframework.http.HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                    ProjectDTO.class);
        }

        ResponseEntity<GetProjectsResponseDTO> response = restTemplate.exchange(
                "/projects?page=0&size=2",
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                GetProjectsResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(5, response.getBody().getTotalElements());
        assertEquals(3, response.getBody().getTotalPages());
    }

    @Test
    void updateProject_shouldReturn204ForValidUpdate() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
        ResponseEntity<ProjectDTO> createdProjectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                ProjectDTO.class);
        var createdProject = createdProjectResponse.getBody();

        var updateRequest = new UpdateProjectRequestDTO();
        updateRequest.setName("Updated Name_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        updateRequest.setDescription("Updated Description_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/projects/" + createdProject.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateRequest, createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void updateProject_shouldReturn404ForNonExistentProject() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var updateRequest = new UpdateProjectRequestDTO();
        updateRequest.setName("Updated Name_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));

        ResponseEntity<String> response = restTemplate.exchange(
                "/projects/" + java.util.UUID.randomUUID(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateRequest, createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void archiveProject_shouldReturn204() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
        ResponseEntity<ProjectDTO> createdProjectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                ProjectDTO.class);
        var createdProject = createdProjectResponse.getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/projects/" + createdProject.getId() + "?action=archive",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void archiveProject_withRejectUnfinishedTasksTrue_shouldRejectTasks() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
        ResponseEntity<ProjectDTO> createdProjectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                ProjectDTO.class);
        var createdProject = createdProjectResponse.getBody();

        var addTaskRequest = new dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(createdProject.getId());
        restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.TaskDTO.class);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/projects/" + createdProject.getId() + "?action=archive&rejectUnfinishedTasks=true",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        var tasksResponse = restTemplate.exchange(
                "/tasks?projectId=" + createdProject.getId(),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO.class);

        assertEquals(HttpStatus.OK, tasksResponse.getStatusCode());
        assertTrue(tasksResponse.getBody().getContent().stream().allMatch(t -> 
                t.getStatus() == dpr.playground.taskprovider.tasks.model.TaskStatusDTO.REJECTED));
    }

    @Test
    void archiveProject_withRejectUnfinishedTasksFalse_shouldNotRejectTasks() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
        ResponseEntity<ProjectDTO> createdProjectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                ProjectDTO.class);
        var createdProject = createdProjectResponse.getBody();

        var addTaskRequest = new dpr.playground.taskprovider.tasks.model.AddTaskRequestDTO();
        addTaskRequest.setSummary(TestDataGenerator.TaskGenerator.randomTaskSummary());
        addTaskRequest.setProjectId(createdProject.getId());
        restTemplate.exchange(
                "/tasks",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(addTaskRequest, createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.TaskDTO.class);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/projects/" + createdProject.getId() + "?action=archive&rejectUnfinishedTasks=false",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        var tasksResponse = restTemplate.exchange(
                "/tasks?projectId=" + createdProject.getId(),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO.class);

        assertEquals(HttpStatus.OK, tasksResponse.getStatusCode());
        assertTrue(tasksResponse.getBody().getContent().stream().allMatch(t -> 
                t.getStatus() == dpr.playground.taskprovider.tasks.model.TaskStatusDTO.NEW));
    }

    @Test
    void restoreProject_shouldReturn204() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        var createProjectRequest = TestDataGenerator.ProjectGenerator.randomProjectRequestDTO();
        ResponseEntity<ProjectDTO> createdProjectResponse = restTemplate.exchange(
                "/projects",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createProjectRequest, createBearerAuthHeaders(loginResponse.getToken())),
                ProjectDTO.class);
        var createdProject = createdProjectResponse.getBody();

        restTemplate.exchange(
                "/projects/" + createdProject.getId() + "?action=archive",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/projects/" + createdProject.getId() + "?action=restore",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void archiveProject_shouldReturn404ForNonExistentProject() throws URISyntaxException {
        var createUserDTO = TestDataGenerator.UserGenerator.randomUserDTO();
        var user = createUserSuccessfully(createUserDTO);
        var loginResponse = loginSuccessfully(createUserDTO.getUsername(), createUserDTO.getPassword());

        ResponseEntity<String> response = restTemplate.exchange(
                "/projects/" + java.util.UUID.randomUUID() + "?action=archive",
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createBearerAuthHeaders(loginResponse.getToken())),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
