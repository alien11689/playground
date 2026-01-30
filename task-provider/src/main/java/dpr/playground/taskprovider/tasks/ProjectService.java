package dpr.playground.taskprovider.tasks;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final Clock clock;

    public Project createProject(String name, String description) {
        var project = Project.create(name, description, clock);
        return projectRepository.save(project);
    }

    public Optional<Project> getProject(UUID id) {
        return projectRepository.findById(id);
    }

    public Optional<Project> updateProject(UUID id, String name, String description) {
        var project = projectRepository.findById(id);
        if (project.isEmpty()) {
            return Optional.empty();
        }

        project.get().update(name, description, clock);
        return Optional.of(projectRepository.save(project.get()));
    }

    public Page<Project> listProjects(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    public Optional<Project> archiveProject(UUID id, boolean rejectUnfinishedTasks) {
        var project = projectRepository.findById(id);
        if (project.isEmpty()) {
            return Optional.empty();
        }

        if (rejectUnfinishedTasks) {
            taskRepository.rejectUnfinishedTasks(id);
        }

        project.get().archive(clock);
        return Optional.of(projectRepository.save(project.get()));
    }

    public Optional<Project> restoreProject(UUID id) {
        var project = projectRepository.findById(id);
        if (project.isEmpty()) {
            return Optional.empty();
        }

        project.get().restore(clock);
        return Optional.of(projectRepository.save(project.get()));
    }

    public boolean isProjectActive(UUID projectId) {
        var project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            throw new ProjectNotFoundException("Project not found: " + projectId);
        }

        if (project.get().getStatus() == ProjectStatus.ARCHIVED) {
            throw new ProjectArchivedException("Project is archived: " + projectId);
        }

        return true;
    }
}
