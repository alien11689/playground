package dpr.playground.taskprovider.user;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import dpr.playground.taskprovider.tasks.model.UserDTO;

public interface UserRepository extends CrudRepository<User, UUID> {
    User getUserByUsername(String username);

    @Query("""
            SELECT new dpr.playground.taskprovider.tasks.model.UserDTO(u.id, u.username, u.firstName,u.lastName)
            FROM User u
            """)
    List<UserDTO> getAllView();
}
