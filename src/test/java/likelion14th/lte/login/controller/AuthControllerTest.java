package likelion14th.lte.login.controller;

import jakarta.servlet.http.HttpServletResponse;
import likelion14th.lte.login.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthControllerTest {

    @Test
    void createRefreshTokenCookie_sets_http_only_cookie_with_refresh_expiration() {
        AuthController controller = new AuthController(mock(AuthService.class));
        ReflectionTestUtils.setField(controller, "cookieSecure", false);
        ReflectionTestUtils.setField(controller, "cookieSamSite", "Lax");
        ReflectionTestUtils.setField(controller, "refreshExpMs", 1_209_600_000L);

        ResponseCookie cookie = controller.createRefreshTokenCookie("refresh-token");

        assertThat(cookie.getName()).isEqualTo("refresh_token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge().toMillis()).isEqualTo(1_209_600_000L);
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    void withdraw_deletes_the_authenticated_user_and_expires_the_refresh_cookie() {
        AuthService authService = mock(AuthService.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthController controller = new AuthController(authService);
        ReflectionTestUtils.setField(controller, "cookieSecure", false);
        ReflectionTestUtils.setField(controller, "cookieSamSite", "Lax");

        Instant now = Instant.now();
        Jwt jwt = new Jwt(
                "access-token",
                now,
                now.plusSeconds(60),
                Map.of("alg", "HS256"),
                Map.of("sub", "42", "type", "ACCESS")
        );

        var result = controller.withdraw(jwt, response);

        verify(authService).withdraw(42L);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Max-Age=0"));
        assertThat(result.getCode()).isEqualTo("USER_2003");
    }
}
