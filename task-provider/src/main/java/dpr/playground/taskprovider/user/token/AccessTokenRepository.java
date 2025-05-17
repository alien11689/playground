package dpr.playground.taskprovider.user.token;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import dpr.playground.taskprovider.auth.LoggedUser;

public interface AccessTokenRepository extends CrudRepository<AccessToken, UUID> {
    @Query("""
            SELECT new dpr.playground.taskprovider.auth.LoggedUser(u.id, u.username, u.password)
            FROM AccessToken at
            JOIN User u on u.id = at.userId  
            where at.token = :token
            """)
    Optional<LoggedUser> findLoggedUserByToken(UUID token);
}
