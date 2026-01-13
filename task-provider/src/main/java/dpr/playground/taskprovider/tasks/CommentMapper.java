package dpr.playground.taskprovider.tasks;

import dpr.playground.taskprovider.tasks.model.CommentDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "addedBy", source = "createdBy")
    CommentDTO toDto(Comment comment);

    default dpr.playground.taskprovider.tasks.model.GetTaskCommentsResponseDTO toGetTaskCommentsResponse(Page<Comment> page) {
        var commentDtos = page.getContent().stream().map(this::toDto).toList();

        var response = new dpr.playground.taskprovider.tasks.model.GetTaskCommentsResponseDTO();
        response.setContent(commentDtos);
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setSize(page.getSize());
        response.setNumber(page.getNumber());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}
