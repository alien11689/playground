package dpr.playground.taskprovider.user;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, UUID> {
    User getUserByUsername(String username);
}
