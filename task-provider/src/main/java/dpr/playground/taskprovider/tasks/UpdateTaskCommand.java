package dpr.playground.taskprovider.tasks;

import java.util.Optional;
import java.util.UUID;

import dpr.playground.taskprovider.tasks.model.TaskStatusDTO;

public record UpdateTaskCommand(Optional<String> summary, Optional<String> description, Optional<TaskStatusDTO> status, Optional<UUID> assignee, UUID updatedBy) {
}
