package dpr.playground.taskprovider.user.token;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import dpr.playground.taskprovider.user.User;

public interface AccessTokenRepository extends CrudRepository<AccessToken, UUID> {
    @Query("""
            SELECT u 
            FROM AccessToken at
            JOIN User u on u.id = at.userId  
            where at.token = :token
            """)
    Optional<User> findUserByToken(UUID token);
}
