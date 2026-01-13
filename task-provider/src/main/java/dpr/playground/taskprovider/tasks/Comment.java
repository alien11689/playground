package dpr.playground.taskprovider.tasks;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "task_comment")
@NoArgsConstructor
@Getter
public class Comment {
    @Id
    @With
    private UUID id;

    @With
    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    @Column(name = "comment")
    private String content;

    public static Comment create(UUID taskId, String content, UUID createdBy, Clock clock) {
        var now = OffsetDateTime.now(clock);
        return new Comment(
                UUID.randomUUID(),
                taskId,
                now,
                createdBy,
                now,
                content);
    }

    public void update(String content, Clock clock) {
        this.content = content;
        this.updatedAt = OffsetDateTime.now(clock);
    }

    private Comment(UUID id, UUID taskId, OffsetDateTime createdAt, UUID createdBy, OffsetDateTime updatedAt, String content) {
        this.id = id;
        this.taskId = taskId;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.content = content;
    }
}
