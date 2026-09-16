package likelion14th.lte.login.jwt;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtProviderTest {

    @Test
    void createRefreshToken_uses_refresh_token_expiration() {
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        JwtDecoder jwtDecoder = mock(JwtDecoder.class);
        Instant now = Instant.now();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new Jwt(
                        "refresh-token",
                        now,
                        now.plusSeconds(60),
                        Map.of("alg", "HS256"),
                        Map.of("sub", "1", "type", "REFRESH")
                ));

        JwtProvider jwtProvider = new JwtProvider(jwtEncoder, jwtDecoder, 1_000L, 10_000L);
        Instant startedAt = Instant.now();

        jwtProvider.createRefreshToken(1L);

        ArgumentCaptor<JwtEncoderParameters> parametersCaptor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parametersCaptor.capture());

        JwtClaimsSet claims = parametersCaptor.getValue().getClaims();
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.getClaimAsString("type")).isEqualTo("REFRESH");
        assertThat(claims.getExpiresAt())
                .isBetween(startedAt.plusMillis(9_000L), startedAt.plusMillis(11_000L));
    }
}
