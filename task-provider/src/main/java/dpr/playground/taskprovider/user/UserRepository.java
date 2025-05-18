package dpr.playground.taskprovider.user;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import dpr.playground.taskprovider.tasks.model.UserDTO;

public interface UserRepository extends CrudRepository<User, UUID>, UserDetailsService {

    @Query("""
            SELECT new dpr.playground.taskprovider.auth.LoggedUser(u.id, u.username, u.password)
            FROM User u
            WHERE u.username = :username
            """)
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    @Query("""
            SELECT new dpr.playground.taskprovider.tasks.model.UserDTO(u.id, u.username, u.firstName,u.lastName)
            FROM User u
            """)
    Page<UserDTO> getAllView(Pageable pageable);
}
