package dpr.playground.taskprovider.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(value = {LoginController.class, SecurityConfig.class, LoginEntryPoint.class, UserDetailsServiceConfig.class, TokenAuthenticationFilter.class})
class LoginControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        mvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("user", "password"))
        ).andExpect(result -> {
            assertEquals(200, result.getResponse().getStatus());
        });
    }

    @Test
    void shouldDenyLoginWithUnknownPassword() throws Exception {
        mvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("user", UUID.randomUUID().toString()))
        ).andExpect(result -> {
            assertEquals(401, result.getResponse().getStatus());
        });
    }
}