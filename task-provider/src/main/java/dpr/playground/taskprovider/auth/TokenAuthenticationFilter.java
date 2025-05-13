package dpr.playground.taskprovider.auth;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import dpr.playground.taskprovider.user.User;
import dpr.playground.taskprovider.user.token.AccessTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenRepository accessTokenRepository;

    public TokenAuthenticationFilter(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        Optional<User> maybeUser = accessTokenRepository.findUserByToken(UUID.fromString(token)); // TODO check token is uuid

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            maybeUser.ifPresent(user -> {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            });
        }

        filterChain.doFilter(request, response);
    }
}
