package dpr.playground.taskprovider.user.token;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "access_token")
public class AccessToken {
    private @Id UUID token;
    private UUID userId;
    private Instant expiresAt;

    protected AccessToken() {
    }

    public AccessToken(UUID token, UUID userId, Instant expiresAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public UUID getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
