package dpr.playground.taskprovider.tasks;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project")
@NoArgsConstructor
@Getter
public class Project {
    @Id
    private UUID id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public static Project create(String name, String description, Clock clock) {
        var now = OffsetDateTime.now(clock);
        var project = new Project();
        project.setId(UUID.randomUUID());
        project.setName(name);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        return project;
    }

    public void update(String name, String description, Clock clock) {
        this.name = name;
        this.description = description;
        this.updatedAt = OffsetDateTime.now(clock);
    }

    public void archive(Clock clock) {
        this.status = ProjectStatus.ARCHIVED;
        this.updatedAt = OffsetDateTime.now(clock);
    }

    public void restore(Clock clock) {
        this.status = ProjectStatus.ACTIVE;
        this.updatedAt = OffsetDateTime.now(clock);
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
