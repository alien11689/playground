package dpr.playground.taskprovider.user;

import java.time.Clock;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dpr.playground.taskprovider.tasks.model.CreateUserDTO;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    public User createUser(CreateUserDTO createUserDTO) {
        // TODO create domain record for create user
        var user = new User(
                UUID.randomUUID(),
                createUserDTO.getUsername(),
                passwordEncoder.encode(createUserDTO.getPassword()),
                createUserDTO.getFirstName(),
                createUserDTO.getLastName(),
                clock.instant());
        return userRepository.save(user);
    }
}
