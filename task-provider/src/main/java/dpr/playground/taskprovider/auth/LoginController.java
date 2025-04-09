package dpr.playground.taskprovider.auth;

import dpr.playground.taskprovider.tasks.api.LoginApi;
import dpr.playground.taskprovider.tasks.model.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class LoginController implements LoginApi {
    @Override
    public ResponseEntity<LoginResponse> login() {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(UUID.randomUUID().toString());
        return ResponseEntity.ok(loginResponse);
    }
}
