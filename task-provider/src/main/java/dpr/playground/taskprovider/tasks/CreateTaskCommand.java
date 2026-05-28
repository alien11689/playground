package dpr.playground.taskprovider.tasks;

import java.util.UUID;

public record CreateTaskCommand(String summary, String description, UUID createdBy, UUID projectId) {
}
