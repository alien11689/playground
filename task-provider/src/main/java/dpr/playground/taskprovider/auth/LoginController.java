package dpr.playground.taskprovider.auth;

import dpr.playground.taskprovider.Database;
import dpr.playground.taskprovider.tasks.api.LoginApi;
import dpr.playground.taskprovider.tasks.model.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class LoginController implements LoginApi {
    @Override
    public ResponseEntity<LoginResponse> login() {
        LoginResponse loginResponse = new LoginResponse();
        String token = UUID.randomUUID().toString();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Database.saveToken(token, user.getUsername());
        loginResponse.setToken(token);
        return ResponseEntity.ok(loginResponse);
    }
}
