package dpr.playground.taskprovider.user;

import java.time.Clock;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public User createUser(CreateUserCommand createUserCommand) {
        var user = new User(
                UUID.randomUUID(),
                createUserCommand.userName(),
                passwordEncoder.encode(createUserCommand.password()),
                createUserCommand.firstName(),
                createUserCommand.lastName(),
                clock.instant());
        return userRepository.save(user);
    }
}
