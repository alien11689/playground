package dpr.playground.taskprovider.endpoint;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dpr.playground.taskprovider.tasks.api.UsersApi;
import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.GetUsersResponseDTO;
import dpr.playground.taskprovider.tasks.model.UserDTO;
import dpr.playground.taskprovider.user.CreateUserCommand;
import dpr.playground.taskprovider.user.UserRepository;
import dpr.playground.taskprovider.user.UserService;

@RestController
class UserController implements UsersApi {
    private final UserService userService;
    private final UserRepository userRepository;

    UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<UserDTO> createUser(CreateUserDTO createUserDTO) {
        var createUserCommand = new CreateUserCommand(
                createUserDTO.getUsername(),
                createUserDTO.getPassword(),
                createUserDTO.getFirstName(),
                createUserDTO.getLastName()
        );
        var user = userService.createUser(createUserCommand);
        return new ResponseEntity<>(new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        ), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<GetUsersResponseDTO> getUsers(Integer page, Integer size) {
        var pageable = PageRequest.of(page == null ? 0 : page, size == null ? 10 : size);
        var usersPage = userRepository.getAllView(pageable);
        return ResponseEntity.ok(new GetUsersResponseDTO()
                .content(usersPage.getContent())
                .size(usersPage.getSize())
                .last(usersPage.isLast())
                .first(usersPage.isFirst())
                .number(usersPage.getNumber())
                .totalPages(usersPage.getTotalPages())
        );
    }
}
