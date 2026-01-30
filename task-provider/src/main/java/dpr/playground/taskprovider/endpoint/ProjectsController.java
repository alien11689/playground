package dpr.playground.taskprovider.endpoint;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dpr.playground.taskprovider.tasks.ProjectMapper;
import dpr.playground.taskprovider.tasks.ProjectNotFoundException;
import dpr.playground.taskprovider.tasks.ProjectService;
import dpr.playground.taskprovider.tasks.api.ProjectsApi;
import dpr.playground.taskprovider.tasks.model.CreateProjectRequestDTO;
import dpr.playground.taskprovider.tasks.model.GetProjectsResponseDTO;
import dpr.playground.taskprovider.tasks.model.ProjectDTO;
import dpr.playground.taskprovider.tasks.model.UpdateProjectRequestDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
class ProjectsController implements ProjectsApi {
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @Override
    public ResponseEntity<ProjectDTO> createProject(CreateProjectRequestDTO createProjectRequestDTO) {
        var project = projectService.createProject(
                createProjectRequestDTO.getName(),
                createProjectRequestDTO.getDescription()
        );
        return new ResponseEntity<>(projectMapper.toDto(project), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProjectDTO> getProject(UUID projectId) {
        var project = projectService.getProject(projectId);
        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projectMapper.toDto(project.get()));
    }

    @Override
    public ResponseEntity<GetProjectsResponseDTO> getProjects(Integer page, Integer size) {
        var pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        var projectsPage = projectService.listProjects(pageable);
        return ResponseEntity.ok(projectMapper.toGetProjectsResponse(projectsPage));
    }

    @Override
    public ResponseEntity<Void> manageProjectStatus(UUID projectId, String action, Boolean rejectUnfinishedTasks) {
        if (rejectUnfinishedTasks == null) {
            rejectUnfinishedTasks = false;
        }

        var project = switch (action) {
            case "archive" -> projectService.archiveProject(projectId, rejectUnfinishedTasks);
            case "restore" -> projectService.restoreProject(projectId);
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        };

        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateProject(UUID projectId, UpdateProjectRequestDTO updateProjectRequestDTO) {
        var project = projectService.updateProject(
                projectId,
                updateProjectRequestDTO.getName(),
                updateProjectRequestDTO.getDescription()
        );
        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
