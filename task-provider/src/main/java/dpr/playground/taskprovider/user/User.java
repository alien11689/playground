package dpr.playground.taskprovider.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "`user`")
public class User {
    private @Id UUID id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Instant createdAt;

    protected User() {
    }

    public User(UUID id, String username, String password, String firstName, String lastName, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
