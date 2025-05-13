package dpr.playground.taskprovider.user;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dpr.playground.taskprovider.tasks.model.CreateUserDTO;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserDTO createUserDTO) {
        // TODO create domain record for create user
        var user = new User(
                UUID.randomUUID(),
                createUserDTO.getUsername(),
                passwordEncoder.encode(createUserDTO.getPassword()),
                createUserDTO.getFirstName(),
                createUserDTO.getLastName(),
                Instant.now()); // TODO use clock
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.getUserByUsername(username);
    }
}
