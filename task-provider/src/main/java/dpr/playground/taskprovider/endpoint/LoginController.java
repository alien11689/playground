package dpr.playground.taskprovider.endpoint;

import java.time.Clock;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import dpr.playground.taskprovider.auth.LoggedUser;
import dpr.playground.taskprovider.tasks.api.LoginApi;
import dpr.playground.taskprovider.tasks.model.LoginResponseDTO;
import dpr.playground.taskprovider.user.token.AccessToken;
import dpr.playground.taskprovider.user.token.AccessTokenRepository;

@RestController
class LoginController implements LoginApi {

    private final AccessTokenRepository accessTokenRepository;
    private final Clock clock;

    LoginController(AccessTokenRepository accessTokenRepository, Clock clock) {
        this.accessTokenRepository = accessTokenRepository;
        this.clock = clock;
    }

    @Override
    public ResponseEntity<LoginResponseDTO> login() {
        var token = UUID.randomUUID();
        var user = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var accessToken = new AccessToken(token, user.getId(), clock.instant());
        accessTokenRepository.save(accessToken);
        var loginResponse = new LoginResponseDTO(token.toString());
        return ResponseEntity.ok(loginResponse);
    }
}
