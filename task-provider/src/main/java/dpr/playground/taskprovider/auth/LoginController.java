package dpr.playground.taskprovider.auth;

import dpr.playground.taskprovider.Database;
import dpr.playground.taskprovider.tasks.api.LoginApi;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class LoginController implements LoginApi {
    @Override
    public ResponseEntity<LoginResponseDTO> login() {
        LoginResponseDTO loginResponse = new LoginResponseDTO();
        String token = UUID.randomUUID().toString();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Database.saveToken(token, user.getUsername());
        loginResponse.setToken(token);
        return ResponseEntity.ok(loginResponse);
    }
}
