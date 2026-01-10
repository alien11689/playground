package dpr.playground.taskprovider.tasks;

import dpr.playground.taskprovider.tasks.model.TaskDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "assignee", ignore = true)
    TaskDTO toDto(Task task);

    TaskDTO toDtoWithAssignee(Task task);

    default dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO toGetTasksResponse(Page<Task> page) {
        var taskDtos = page.getContent().stream().map(this::toDtoWithAssignee).toList();

        var response = new dpr.playground.taskprovider.tasks.model.GetTasksResponseDTO();
        response.setContent(taskDtos);
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setSize(page.getSize());
        response.setNumber(page.getNumber());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}
