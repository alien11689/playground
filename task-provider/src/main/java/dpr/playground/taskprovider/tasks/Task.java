package dpr.playground.taskprovider.tasks;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import dpr.playground.taskprovider.tasks.model.TaskStatusDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "task")
@NoArgsConstructor
@Getter
public class Task {
    @Id
    @With
    private UUID id;

    @With
    private String summary;

    @With
    private String description;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    private UUID createdBy;
    @Column(name = "last_updated_at")
    private OffsetDateTime updatedAt;
    @Column(name = "last_updated_by")
    private UUID updatedBy;

    @Enumerated(EnumType.STRING)
    @With
    private TaskStatusDTO status;

    @With
    @Column(name = "assigned_to")
    private UUID assignee;

    @ManyToOne
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    public UUID getProjectId() {
        return project != null ? project.getId() : null;
    }

    public static Task create(CreateTaskCommand command, Clock clock) {
        var now = OffsetDateTime.now(clock);
        return new Task(
                UUID.randomUUID(),
                command.summary(),
                command.description(),
                now,
                command.createdBy(),
                now,
                command.createdBy(),
                TaskStatusDTO.NEW,
                null,
                null);
    }

    public void update(UpdateTaskCommand command, Clock clock) {
        command.summary().ifPresent(value -> this.summary = value);
        command.description().ifPresent(value -> this.description = value);
        command.status().ifPresent(value -> this.status = value);
        command.assignee().ifPresent(value -> this.assignee = value);

        this.updatedAt = OffsetDateTime.now(clock);
        this.updatedBy = command.updatedBy();
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    private Task(UUID id, String summary, String description, OffsetDateTime createdAt, UUID createdBy, OffsetDateTime updatedAt, UUID updatedBy, TaskStatusDTO status, UUID assignee, Project project) {
        this.id = id;
        this.summary = summary;
        this.description = description;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.status = status;
        this.assignee = assignee;
        this.project = project;
    }
}
