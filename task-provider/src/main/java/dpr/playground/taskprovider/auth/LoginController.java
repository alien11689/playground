package dpr.playground.taskprovider.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import dpr.playground.taskprovider.tasks.api.LoginApi;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import dpr.playground.taskprovider.user.User;
import dpr.playground.taskprovider.user.token.AccessToken;
import dpr.playground.taskprovider.user.token.AccessTokenRepository;

@RestController
class LoginController implements LoginApi {

    private final AccessTokenRepository accessTokenRepository;

    LoginController(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    public ResponseEntity<LoginResponseDTO> login() {
        LoginResponseDTO loginResponse = new LoginResponseDTO();
        UUID token = UUID.randomUUID();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var accessToken = new AccessToken(token, user.getId(), Instant.now()); // TODO use clock
        accessTokenRepository.save(accessToken);
        loginResponse.setToken(token.toString());
        return ResponseEntity.ok(loginResponse);
    }
}
