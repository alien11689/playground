package dpr.playground.taskprovider.tasks;

import dpr.playground.taskprovider.tasks.model.GetProjectsResponseDTO;
import dpr.playground.taskprovider.tasks.model.ProjectDTO;
import dpr.playground.taskprovider.tasks.model.ProjectStatusDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    default ProjectDTO toDto(Project project) {
        return new ProjectDTO()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(mapStatus(project.getStatus()))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt());
    }

    default GetProjectsResponseDTO toGetProjectsResponse(Page<Project> page) {
        var projectDtos = page.getContent().stream().map(this::toDto).toList();

        var response = new GetProjectsResponseDTO();
        response.setContent(projectDtos);
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setSize(page.getSize());
        response.setNumber(page.getNumber());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }

    private ProjectStatusDTO mapStatus(ProjectStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case ACTIVE -> ProjectStatusDTO.ACTIVE;
            case ARCHIVED -> ProjectStatusDTO.ARCHIVED;
        };
    }
}
