package dpr.playground.taskprovider;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dpr.playground.taskprovider.tasks.api.UsersApi;
import dpr.playground.taskprovider.tasks.model.CreateUserDTO;
import dpr.playground.taskprovider.tasks.model.GetUsersResponseDTO;
import dpr.playground.taskprovider.tasks.model.UserDTO;
import dpr.playground.taskprovider.user.UserService;

@RestController
public class UserController implements UsersApi {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserDTO> createUser(CreateUserDTO createUserDTO) {
        var user = userService.createUser(createUserDTO);
        return new ResponseEntity<>(new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        ), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<GetUsersResponseDTO> getUsers(Integer page, Integer size) {
        throw new RuntimeException("Not implemented");
    }
}
